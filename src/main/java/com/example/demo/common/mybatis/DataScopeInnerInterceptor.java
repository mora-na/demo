package com.example.demo.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.*;

public class DataScopeInnerInterceptor implements InnerInterceptor {

    private final DataScopeProperties properties;
    private final DataScopeRuleProvider ruleProvider;

    public DataScopeInnerInterceptor(DataScopeProperties properties, DataScopeRuleProvider ruleProvider) {
        this.properties = properties;
        this.ruleProvider = ruleProvider;
    }

    @Override
    public void beforePrepare(StatementHandler statementHandler, Connection connection, Integer transactionTimeout) {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        AuthUser user = AuthContext.get();
        if (user == null) {
            return;
        }
        BoundSql boundSql = statementHandler.getBoundSql();
        if (boundSql == null) {
            return;
        }
        String sql = boundSql.getSql();
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }
        String dataScopeType = normalizeType(user.getDataScopeType(), properties.getDefaultType());
        if (DataScopeType.ALL.equals(dataScopeType)) {
            return;
        }
        Map<String, String> tableColumnMap = ruleProvider == null ? null : ruleProvider.getTableColumnMap();
        if (tableColumnMap == null || tableColumnMap.isEmpty()) {
            return;
        }
        String rewritten = rewriteSql(sql, dataScopeType, user, tableColumnMap);
        if (rewritten != null && !rewritten.equals(sql)) {
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql", rewritten);
        }
    }

    private String rewriteSql(String sql, String dataScopeType, AuthUser user, Map<String, String> tableColumnMap) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            boolean changed = applyDataScope(statement, dataScopeType, user, tableColumnMap);
            if (!changed) {
                return sql;
            }
            return statement.toString();
        } catch (Exception ex) {
            return sql;
        }
    }

    private boolean applyDataScope(Statement statement, String dataScopeType, AuthUser user,
                                   Map<String, String> tableColumnMap) {
        if (statement instanceof Select) {
            return applySelect((Select) statement, dataScopeType, user, tableColumnMap);
        }
        if (statement instanceof Update) {
            return applyUpdate((Update) statement, dataScopeType, user, tableColumnMap);
        }
        if (statement instanceof Delete) {
            return applyDelete((Delete) statement, dataScopeType, user, tableColumnMap);
        }
        return false;
    }

    private boolean applySelect(Select select, String dataScopeType, AuthUser user,
                                Map<String, String> tableColumnMap) {
        if (select == null) {
            return false;
        }
        boolean changed = false;
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                changed |= applySelect(withItem, dataScopeType, user, tableColumnMap);
            }
        }
        if (select instanceof PlainSelect) {
            return changed | applyPlainSelect((PlainSelect) select, dataScopeType, user, tableColumnMap);
        }
        if (select instanceof SetOperationList) {
            for (Select subSelect : ((SetOperationList) select).getSelects()) {
                changed |= applySelect(subSelect, dataScopeType, user, tableColumnMap);
            }
            return changed;
        }
        if (select instanceof ParenthesedSelect) {
            Select inner = ((ParenthesedSelect) select).getSelect();
            if (inner != null) {
                changed |= applySelect(inner, dataScopeType, user, tableColumnMap);
            }
            return changed;
        }
        return changed;
    }

    private boolean applyPlainSelect(PlainSelect select, String dataScopeType, AuthUser user,
                                     Map<String, String> tableColumnMap) {
        boolean changed = false;
        changed |= applyFromItem(select.getFromItem(), dataScopeType, user, tableColumnMap);
        if (select.getJoins() != null) {
            for (Join join : select.getJoins()) {
                changed |= applyFromItem(join.getRightItem(), dataScopeType, user, tableColumnMap);
            }
        }
        List<TableRef> tableRefs = collectTableRefs(select.getFromItem(), select.getJoins(), tableColumnMap);
        Expression condition = buildCombinedCondition(tableRefs, dataScopeType, user);
        if (condition == null) {
            return changed;
        }
        if (select.getWhere() == null) {
            select.setWhere(condition);
        } else {
            select.setWhere(new AndExpression(select.getWhere(), condition));
        }
        return true;
    }

    private boolean applyUpdate(Update update, String dataScopeType, AuthUser user,
                                Map<String, String> tableColumnMap) {
        List<TableRef> tableRefs = new ArrayList<>();
        if (update.getTable() != null) {
            addTableRef(update.getTable(), tableRefs, tableColumnMap);
        }
        for (Table table : extractTables(update)) {
            addTableRef(table, tableRefs, tableColumnMap);
        }
        Expression condition = buildCombinedCondition(tableRefs, dataScopeType, user);
        if (condition == null) {
            return false;
        }
        if (update.getWhere() == null) {
            update.setWhere(condition);
        } else {
            update.setWhere(new AndExpression(update.getWhere(), condition));
        }
        return true;
    }

    private boolean applyDelete(Delete delete, String dataScopeType, AuthUser user,
                                Map<String, String> tableColumnMap) {
        List<TableRef> tableRefs = new ArrayList<>();
        if (delete.getTable() != null) {
            addTableRef(delete.getTable(), tableRefs, tableColumnMap);
        }
        for (Table table : extractTables(delete)) {
            addTableRef(table, tableRefs, tableColumnMap);
        }
        Expression condition = buildCombinedCondition(tableRefs, dataScopeType, user);
        if (condition == null) {
            return false;
        }
        if (delete.getWhere() == null) {
            delete.setWhere(condition);
        } else {
            delete.setWhere(new AndExpression(delete.getWhere(), condition));
        }
        return true;
    }

    private boolean applyFromItem(FromItem fromItem, String dataScopeType, AuthUser user,
                                  Map<String, String> tableColumnMap) {
        if (fromItem instanceof ParenthesedSelect) {
            Select inner = ((ParenthesedSelect) fromItem).getSelect();
            return applySelect(inner, dataScopeType, user, tableColumnMap);
        }
        if (fromItem instanceof ParenthesedFromItem) {
            ParenthesedFromItem parenthesed = (ParenthesedFromItem) fromItem;
            boolean changed = applyFromItem(parenthesed.getFromItem(), dataScopeType, user, tableColumnMap);
            if (parenthesed.getJoins() != null) {
                for (Join join : parenthesed.getJoins()) {
                    changed |= applyFromItem(join.getRightItem(), dataScopeType, user, tableColumnMap);
                }
            }
            return changed;
        }
        return false;
    }

    private List<Table> extractTables(Object statement) {
        try {
            java.lang.reflect.Method method = statement.getClass().getMethod("getTables");
            Object value = method.invoke(statement);
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                List<Table> tables = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Table) {
                        tables.add((Table) item);
                    }
                }
                return tables;
            }
        } catch (Exception ignore) {
        }
        return Collections.emptyList();
    }

    private List<TableRef> collectTableRefs(FromItem fromItem, List<Join> joins,
                                            Map<String, String> tableColumnMap) {
        List<TableRef> refs = new ArrayList<>();
        collectTableRefsFromItem(fromItem, refs, tableColumnMap);
        if (joins != null) {
            for (Join join : joins) {
                collectTableRefsFromItem(join.getRightItem(), refs, tableColumnMap);
            }
        }
        return refs;
    }

    private void collectTableRefsFromItem(FromItem fromItem, List<TableRef> refs,
                                          Map<String, String> tableColumnMap) {
        if (fromItem instanceof Table) {
            addTableRef((Table) fromItem, refs, tableColumnMap);
            return;
        }
        if (fromItem instanceof ParenthesedFromItem) {
            ParenthesedFromItem parenthesed = (ParenthesedFromItem) fromItem;
            collectTableRefsFromItem(parenthesed.getFromItem(), refs, tableColumnMap);
            if (parenthesed.getJoins() != null) {
                for (Join join : parenthesed.getJoins()) {
                    collectTableRefsFromItem(join.getRightItem(), refs, tableColumnMap);
                }
            }
        }
    }

    private void addTableRef(Table table, List<TableRef> refs, Map<String, String> tableColumnMap) {
        if (table == null) {
            return;
        }
        String tableName = normalizeName(table.getName());
        String fullName = normalizeName(table.getFullyQualifiedName());
        String column = lookupColumn(tableColumnMap, tableName, fullName);
        if (column == null) {
            return;
        }
        String qualifier = table.getAlias() == null ? table.getName() : table.getAlias().getName();
        refs.add(new TableRef(table.getName(), qualifier, column));
    }

    private Expression buildCombinedCondition(List<TableRef> tableRefs, String dataScopeType, AuthUser user) {
        if (tableRefs == null || tableRefs.isEmpty()) {
            return null;
        }
        List<Expression> expressions = new ArrayList<>();
        for (TableRef ref : tableRefs) {
            Expression condition = buildCondition(dataScopeType, ref, user);
            if (condition != null) {
                expressions.add(condition);
            }
        }
        if (expressions.isEmpty()) {
            return null;
        }
        Expression combined = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            combined = new AndExpression(combined, expressions.get(i));
        }
        return combined;
    }

    private Expression buildCondition(String dataScopeType, TableRef ref, AuthUser user) {
        if (DataScopeType.NONE.equals(dataScopeType)) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        if (DataScopeType.SELF.equals(dataScopeType)) {
            if (user.getId() == null) {
                return new EqualsTo(new LongValue(1), new LongValue(0));
            }
            return new EqualsTo(ref.toColumn(), new LongValue(user.getId()));
        }
        if (DataScopeType.CUSTOM.equals(dataScopeType)) {
            String value = user.getDataScopeValue();
            if (value == null || value.trim().isEmpty()) {
                return new EqualsTo(new LongValue(1), new LongValue(0));
            }
            List<Expression> values = parseValues(value);
            if (values.isEmpty()) {
                return new EqualsTo(new LongValue(1), new LongValue(0));
            }
            InExpression in = new InExpression();
            in.setLeftExpression(ref.toColumn());
            in.setRightExpression(new ExpressionList<>(values));
            return in;
        }
        return null;
    }

    private List<Expression> parseValues(String value) {
        String[] tokens = value.split(",");
        List<Expression> values = new ArrayList<>();
        for (String token : tokens) {
            String trimmed = trimQuotes(token);
            if (trimmed.isEmpty()) {
                continue;
            }
            if (isNumeric(trimmed)) {
                values.add(new LongValue(trimmed));
                continue;
            }
            if (isSafeToken(trimmed)) {
                values.add(new StringValue(trimmed));
            }
        }
        return values;
    }

    private String trimQuotes(String token) {
        if (token == null) {
            return "";
        }
        String trimmed = token.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("'") && trimmed.endsWith("'")) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private boolean isNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isSafeToken(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '-')) {
                return false;
            }
        }
        return true;
    }

    private String lookupColumn(Map<String, String> tableColumnMap, String name, String fullName) {
        if (name != null) {
            String column = tableColumnMap.get(name);
            if (column != null) {
                return column;
            }
        }
        if (fullName != null) {
            return tableColumnMap.get(fullName);
        }
        return null;
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeType(String value, String fallback) {
        String candidate = value;
        if (candidate == null || candidate.trim().isEmpty()) {
            candidate = fallback;
        }
        if (candidate == null) {
            return DataScopeType.ALL;
        }
        return candidate.trim().toUpperCase(Locale.ROOT);
    }

    private static final class TableRef {
        private final String table;
        private final String qualifier;
        private final String column;

        private TableRef(String table, String qualifier, String column) {
            this.table = table;
            this.qualifier = qualifier;
            this.column = column;
        }

        private Column toColumn() {
            Column columnRef = new Column();
            columnRef.setColumnName(column);
            if (qualifier != null && !qualifier.isEmpty()) {
                columnRef.setTable(new Table(qualifier));
            }
            return columnRef;
        }
    }
}
