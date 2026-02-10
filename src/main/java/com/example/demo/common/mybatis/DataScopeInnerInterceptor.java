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

/**
 * 数据权限拦截器，基于 JSqlParser 在 SQL 上拼接数据范围过滤条件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class DataScopeInnerInterceptor implements InnerInterceptor {

    private final DataScopeProperties properties;
    private final DataScopeRuleProvider ruleProvider;

    /**
     * 构建数据权限拦截器。
     *
     * @param properties   数据范围配置
     * @param ruleProvider 数据范围规则提供者
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DataScopeInnerInterceptor(DataScopeProperties properties, DataScopeRuleProvider ruleProvider) {
        this.properties = properties;
        this.ruleProvider = ruleProvider;
    }

    /**
     * 在 SQL 预编译前重写语句，注入数据权限过滤条件。
     *
     * @param statementHandler   StatementHandler
     * @param connection         数据库连接
     * @param transactionTimeout 事务超时（秒）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 解析并重写 SQL，失败时降级为原 SQL。
     *
     * @param sql            原始 SQL
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return 重写后的 SQL
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 在不同语句类型上应用数据权限。
     *
     * @param statement      SQL 语句对象
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 在 SELECT 语句上递归应用数据权限。
     *
     * @param select         Select 语句
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 在普通 SELECT 上拼接 WHERE 数据权限条件。
     *
     * @param select         PlainSelect
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 在 UPDATE 语句上拼接 WHERE 数据权限条件。
     *
     * @param update         Update 语句
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 在 DELETE 语句上拼接 WHERE 数据权限条件。
     *
     * @param delete         Delete 语句
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 递归处理 FROM 子项中的子查询。
     *
     * @param fromItem       FromItem
     * @param dataScopeType  数据范围类型
     * @param user           当前用户
     * @param tableColumnMap 表->字段 映射
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 反射读取语句中的表集合（JSqlParser 差异兼容）。
     *
     * @param statement Select/Update/Delete 语句
     * @return 表列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 收集 FROM/Join 中的表引用列表。
     *
     * @param fromItem       FromItem
     * @param joins          Join 列表
     * @param tableColumnMap 表->字段 映射
     * @return 表引用列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 从 FromItem 递归提取表引用。
     *
     * @param fromItem       FromItem
     * @param refs           表引用结果集
     * @param tableColumnMap 表->字段 映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 添加表引用及其数据权限列映射。
     *
     * @param table          表对象
     * @param refs           表引用结果集
     * @param tableColumnMap 表->字段 映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 组合多个表的权限条件为 AND 表达式。
     *
     * @param tableRefs     表引用列表
     * @param dataScopeType 数据范围类型
     * @param user          当前用户
     * @return 组合条件，若无需限制返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 构建单表的数据权限条件表达式。
     *
     * @param dataScopeType 数据范围类型
     * @param ref           表引用
     * @param user          当前用户
     * @return 条件表达式，若无需限制返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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
        if (DataScopeType.DEPT.equals(dataScopeType)) {
            if (user.getDeptId() == null) {
                return new EqualsTo(new LongValue(1), new LongValue(0));
            }
            return new EqualsTo(ref.toColumn(), new LongValue(user.getDeptId()));
        }
        if (DataScopeType.DEPT_AND_CHILD.equals(dataScopeType)
                || DataScopeType.CUSTOM_DEPT.equals(dataScopeType)
                || DataScopeType.CUSTOM.equals(dataScopeType)) {
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

    /**
     * 解析自定义数据范围值为表达式集合。
     *
     * @param value 逗号分隔的值
     * @return 表达式列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 去除字符串两侧单引号并裁剪空白。
     *
     * @param token 原始值
     * @return 处理后的值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 判断字符串是否为纯数字。
     *
     * @param value 值
     * @return true 表示纯数字
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 判断自定义 token 是否为安全字符集。
     *
     * @param value token 值
     * @return true 表示安全
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isSafeToken(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '-')) {
                return false;
            }
        }
        return true;
    }

    /**
     * 查询表名对应的数据权限列。
     *
     * @param tableColumnMap 表->字段 映射
     * @param name           表名
     * @param fullName       全名
     * @return 列名，未命中返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 规范化名称（trim + lower）。
     *
     * @param name 名称
     * @return 规范化结果
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String normalizeName(String name) {
        return name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 规范化数据范围类型，空值时回退默认类型。
     *
     * @param value    数据范围类型
     * @param fallback 默认类型
     * @return 规范化后的类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 表引用与权限字段映射容器。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static final class TableRef {
        private final String table;
        private final String qualifier;
        private final String column;

        /**
         * 构建表引用。
         *
         * @param table     表名
         * @param qualifier 表别名或限定名
         * @param column    权限字段
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private TableRef(String table, String qualifier, String column) {
            this.table = table;
            this.qualifier = qualifier;
            this.column = column;
        }

        /**
         * 生成带别名的列引用。
         *
         * @return Column 引用
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
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
