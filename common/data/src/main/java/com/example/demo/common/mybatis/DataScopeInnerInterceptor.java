package com.example.demo.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.datascope.service.DataScopeEvaluator;
import lombok.Getter;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
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
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.util.*;

/**
 * 数据权限拦截器，基于 JSqlParser 在 SQL 上拼接数据范围过滤条件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class DataScopeInnerInterceptor implements InnerInterceptor {

    // Guard against recursive rule loading that can exhaust the connection pool.
    private static final ThreadLocal<Boolean> LOADING_RULES = new ThreadLocal<>();
    private static final String DEFAULT_DEPT_COLUMN = "create_dept";
    private static final String DEFAULT_USER_COLUMN = "create_by";
    private static final Logger log = LoggerFactory.getLogger(DataScopeInnerInterceptor.class);
    private static final int MAX_SQL_LOG_LENGTH = 500;

    private final DataScopeProperties properties;
    private final DataScopeRuleProvider ruleProvider;
    private final DataScopeEvaluator evaluator;

    /**
     * 构建数据权限拦截器。
     *
     * @param properties   数据范围配置
     * @param ruleProvider 数据范围规则提供者
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DataScopeInnerInterceptor(DataScopeProperties properties,
                                     DataScopeRuleProvider ruleProvider,
                                     DataScopeEvaluator evaluator) {
        this.properties = properties;
        this.ruleProvider = ruleProvider;
        this.evaluator = evaluator;
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
        if (statementHandler == null) {
            return;
        }
        BoundSql boundSql = statementHandler.getBoundSql();
        if (boundSql == null) {
            return;
        }
        String sql = boundSql.getSql();
        if (isSelectSql(sql)) {
            // SELECT 由 beforeQuery 处理，避免重复改写
            return;
        }
        applyDataScope(boundSql);
    }

    @Override
    public void beforeQuery(Executor executor,
                            MappedStatement ms,
                            Object parameter,
                            RowBounds rowBounds,
                            ResultHandler resultHandler,
                            BoundSql boundSql) {
        if (ms == null || ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }
        applyDataScope(boundSql);
    }

    private void applyDataScope(BoundSql boundSql) {
        if (properties == null || !properties.isEnabled()) {
            return;
        }
        if (Boolean.TRUE.equals(LOADING_RULES.get())) {
            return;
        }
        AuthUser user = AuthContext.get();
        if (user == null) {
            return;
        }
        DataScopeContextHolder.DataScopeRequest scopeRequest = DataScopeContextHolder.get();
        if (scopeRequest == null || !StringUtils.hasText(scopeRequest.getScopeKey())) {
            return;
        }
        String sql = boundSql.getSql();
        if (sql == null || sql.trim().isEmpty()) {
            return;
        }
        DataScopeEvaluator.FinalScope finalScope = evaluator == null
                ? DataScopeEvaluator.FinalScope.none()
                : evaluator.resolve(user, scopeRequest.getScopeKey());
        if (finalScope.isAll()) {
            return;
        }
        Map<String, DataScopeRuleDefinition> ruleMap;
        try {
            LOADING_RULES.set(true);
            ruleMap = ruleProvider == null ? null : ruleProvider.getRuleMap();
        } finally {
            LOADING_RULES.remove();
        }
        DataScopeRuleDefinition rule = ruleMap == null ? null : ruleMap.get(scopeRequest.getScopeKey());
        EffectiveRule effectiveRule = buildEffectiveRule(rule, scopeRequest);
        String rewritten = rewriteSql(sql, finalScope, user, effectiveRule, scopeRequest);
        if (rewritten != null && !rewritten.equals(sql)) {
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql", rewritten);
        }
    }

    private boolean isSelectSql(String sql) {
        if (sql == null) {
            return false;
        }
        String trimmed = sql.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        return lower.startsWith("select") || lower.startsWith("with");
    }

    /**
     * 解析并重写 SQL，失败时降级为原 SQL。
     *
     * @param sql        原始 SQL
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return 重写后的 SQL
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String rewriteSql(String sql,
                              DataScopeEvaluator.FinalScope finalScope,
                              AuthUser user,
                              EffectiveRule rule,
                              DataScopeContextHolder.DataScopeRequest scopeRequest) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            boolean changed = applyDataScope(statement, finalScope, user, rule, scopeRequest);
            if (!changed) {
                return sql;
            }
            return statement.toString();
        } catch (Exception ex) {
            String scopeKey = scopeRequest == null ? null : scopeRequest.getScopeKey();
            String safeSql = limitSql(sql);
            if (properties != null && !properties.isFailOpenOnSqlParseError()) {
                log.warn("DataScope SQL parse failed, blocking query. scopeKey={}, sql={}", scopeKey, safeSql, ex);
                throw new IllegalStateException("DataScope SQL parse failed", ex);
            }
            log.warn("DataScope SQL parse failed, skip rewrite. scopeKey={}, sql={}", scopeKey, safeSql, ex);
            return sql;
        }
    }

    /**
     * 在不同语句类型上应用数据权限。
     *
     * @param statement  SQL 语句对象
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean applyDataScope(Statement statement,
                                   DataScopeEvaluator.FinalScope finalScope,
                                   AuthUser user,
                                   EffectiveRule rule,
                                   DataScopeContextHolder.DataScopeRequest scopeRequest) {
        if (statement instanceof Select) {
            return applySelect((Select) statement, finalScope, user, rule, scopeRequest);
        }
        if (statement instanceof Update) {
            return applyUpdate((Update) statement, finalScope, user, rule, scopeRequest);
        }
        if (statement instanceof Delete) {
            return applyDelete((Delete) statement, finalScope, user, rule, scopeRequest);
        }
        return false;
    }

    /**
     * 在 SELECT 语句上递归应用数据权限。
     *
     * @param select     Select 语句
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean applySelect(Select select,
                                DataScopeEvaluator.FinalScope finalScope,
                                AuthUser user,
                                EffectiveRule rule,
                                DataScopeContextHolder.DataScopeRequest scopeRequest) {
        if (select == null) {
            return false;
        }
        boolean changed = false;
        if (select.getWithItemsList() != null) {
            for (WithItem withItem : select.getWithItemsList()) {
                changed |= applySelect(withItem, finalScope, user, rule, scopeRequest);
            }
        }
        if (select instanceof PlainSelect) {
            return changed | applyPlainSelect((PlainSelect) select, finalScope, user, rule, scopeRequest);
        }
        if (select instanceof SetOperationList) {
            for (Select subSelect : ((SetOperationList) select).getSelects()) {
                changed |= applySelect(subSelect, finalScope, user, rule, scopeRequest);
            }
            return changed;
        }
        if (select instanceof ParenthesedSelect) {
            Select inner = ((ParenthesedSelect) select).getSelect();
            if (inner != null) {
                changed |= applySelect(inner, finalScope, user, rule, scopeRequest);
            }
            return changed;
        }
        return changed;
    }

    private String limitSql(String sql) {
        if (sql == null) {
            return "";
        }
        String trimmed = sql.trim();
        if (trimmed.length() <= MAX_SQL_LOG_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_SQL_LOG_LENGTH) + "...";
    }

    /**
     * 在普通 SELECT 上拼接 WHERE 数据权限条件。
     *
     * @param select     PlainSelect
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean applyPlainSelect(PlainSelect select,
                                     DataScopeEvaluator.FinalScope finalScope,
                                     AuthUser user,
                                     EffectiveRule rule,
                                     DataScopeContextHolder.DataScopeRequest scopeRequest) {
        boolean changed = false;
        changed |= applyFromItem(select.getFromItem(), finalScope, user, rule, scopeRequest);
        if (select.getJoins() != null) {
            for (Join join : select.getJoins()) {
                changed |= applyFromItem(join.getRightItem(), finalScope, user, rule, scopeRequest);
            }
        }
        if (!matchesRule(select.getFromItem(), select.getJoins(), rule, scopeRequest)) {
            return changed;
        }
        String deptQualifier = resolveQualifier(select.getFromItem(), scopeRequest.getDeptAlias(), rule.getTableAlias());
        String userQualifier = resolveQualifier(select.getFromItem(), scopeRequest.getUserAlias(), rule.getTableAlias());
        Expression condition = buildCondition(finalScope, user, rule, deptQualifier, userQualifier);
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
     * @param update     Update 语句
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean applyUpdate(Update update,
                                DataScopeEvaluator.FinalScope finalScope,
                                AuthUser user,
                                EffectiveRule rule,
                                DataScopeContextHolder.DataScopeRequest scopeRequest) {
        Table table = update.getTable();
        if (!matchesRule(table, rule, scopeRequest)) {
            return false;
        }
        String deptQualifier = resolveQualifier(table, scopeRequest.getDeptAlias(), rule.getTableAlias());
        String userQualifier = resolveQualifier(table, scopeRequest.getUserAlias(), rule.getTableAlias());
        Expression condition = buildCondition(finalScope, user, rule, deptQualifier, userQualifier);
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
     * @param delete     Delete 语句
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean applyDelete(Delete delete,
                                DataScopeEvaluator.FinalScope finalScope,
                                AuthUser user,
                                EffectiveRule rule,
                                DataScopeContextHolder.DataScopeRequest scopeRequest) {
        Table table = delete.getTable();
        if (!matchesRule(table, rule, scopeRequest)) {
            return false;
        }
        String deptQualifier = resolveQualifier(table, scopeRequest.getDeptAlias(), rule.getTableAlias());
        String userQualifier = resolveQualifier(table, scopeRequest.getUserAlias(), rule.getTableAlias());
        Expression condition = buildCondition(finalScope, user, rule, deptQualifier, userQualifier);
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
     * @param fromItem   FromItem
     * @param finalScope 最终数据范围
     * @param user       当前用户
     * @param rule       生效规则
     * @return true 表示发生了改写
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean applyFromItem(FromItem fromItem,
                                  DataScopeEvaluator.FinalScope finalScope,
                                  AuthUser user,
                                  EffectiveRule rule,
                                  DataScopeContextHolder.DataScopeRequest scopeRequest) {
        if (fromItem instanceof ParenthesedSelect) {
            Select inner = ((ParenthesedSelect) fromItem).getSelect();
            return applySelect(inner, finalScope, user, rule, scopeRequest);
        }
        if (fromItem instanceof ParenthesedFromItem) {
            ParenthesedFromItem parenthesed = (ParenthesedFromItem) fromItem;
            boolean changed = applyFromItem(parenthesed.getFromItem(), finalScope, user, rule, scopeRequest);
            if (parenthesed.getJoins() != null) {
                for (Join join : parenthesed.getJoins()) {
                    changed |= applyFromItem(join.getRightItem(), finalScope, user, rule, scopeRequest);
                }
            }
            return changed;
        }
        return false;
    }

    /**
     * 判断当前查询是否匹配规则。
     */
    private boolean matchesRule(FromItem fromItem, List<Join> joins, EffectiveRule rule,
                                DataScopeContextHolder.DataScopeRequest scopeRequest) {
        if (rule == null) {
            return false;
        }
        if (!StringUtils.hasText(rule.getTableName()) && !StringUtils.hasText(rule.getTableAlias())) {
            return true;
        }
        List<Table> tables = collectTables(fromItem, joins);
        if (tables.isEmpty()) {
            return false;
        }
        String expectedAlias = normalizeName(rule.getTableAlias());
        String expectedTable = normalizeName(rule.getTableName());
        for (Table table : tables) {
            if (table == null) {
                continue;
            }
            if (StringUtils.hasText(expectedAlias)) {
                String alias = table.getAlias() == null ? null : table.getAlias().getName();
                if (expectedAlias.equalsIgnoreCase(normalizeName(alias))) {
                    return true;
                }
            }
            if (StringUtils.hasText(expectedTable)) {
                String name = normalizeName(table.getName());
                String full = normalizeName(table.getFullyQualifiedName());
                if (expectedTable.equalsIgnoreCase(name) || expectedTable.equalsIgnoreCase(full)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesRule(Table table, EffectiveRule rule, DataScopeContextHolder.DataScopeRequest scopeRequest) {
        if (rule == null) {
            return false;
        }
        if (table == null) {
            return false;
        }
        String expectedAlias = normalizeName(rule.getTableAlias());
        String expectedTable = normalizeName(rule.getTableName());
        if (!StringUtils.hasText(expectedAlias) && !StringUtils.hasText(expectedTable)) {
            return true;
        }
        if (StringUtils.hasText(expectedAlias)) {
            String alias = table.getAlias() == null ? null : table.getAlias().getName();
            if (expectedAlias.equalsIgnoreCase(normalizeName(alias))) {
                return true;
            }
        }
        if (StringUtils.hasText(expectedTable)) {
            String name = normalizeName(table.getName());
            String full = normalizeName(table.getFullyQualifiedName());
            return expectedTable.equalsIgnoreCase(name) || expectedTable.equalsIgnoreCase(full);
        }
        return false;
    }

    private List<Table> collectTables(FromItem fromItem, List<Join> joins) {
        List<Table> tables = new ArrayList<>();
        collectTablesFromItem(fromItem, tables);
        if (joins != null) {
            for (Join join : joins) {
                collectTablesFromItem(join.getRightItem(), tables);
            }
        }
        return tables;
    }

    private void collectTablesFromItem(FromItem fromItem, List<Table> tables) {
        if (fromItem instanceof Table) {
            tables.add((Table) fromItem);
            return;
        }
        if (fromItem instanceof ParenthesedFromItem) {
            ParenthesedFromItem parenthesed = (ParenthesedFromItem) fromItem;
            collectTablesFromItem(parenthesed.getFromItem(), tables);
            if (parenthesed.getJoins() != null) {
                for (Join join : parenthesed.getJoins()) {
                    collectTablesFromItem(join.getRightItem(), tables);
                }
            }
        }
    }

    private String resolveQualifier(FromItem fromItem, String preferredAlias, String ruleAlias) {
        if (StringUtils.hasText(preferredAlias)) {
            return preferredAlias;
        }
        if (StringUtils.hasText(ruleAlias)) {
            return ruleAlias;
        }
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            if (table.getAlias() != null) {
                return table.getAlias().getName();
            }
            return table.getName();
        }
        return null;
    }

    private String resolveQualifier(Table table, String preferredAlias, String ruleAlias) {
        if (StringUtils.hasText(preferredAlias)) {
            return preferredAlias;
        }
        if (StringUtils.hasText(ruleAlias)) {
            return ruleAlias;
        }
        if (table == null) {
            return null;
        }
        if (table.getAlias() != null) {
            return table.getAlias().getName();
        }
        return table.getName();
    }

    private Expression buildCondition(DataScopeEvaluator.FinalScope finalScope,
                                      AuthUser user,
                                      EffectiveRule rule,
                                      String deptQualifier,
                                      String userQualifier) {
        if (finalScope == null || finalScope.isNone()) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        if (finalScope.isAll()) {
            return null;
        }
        String deptColumn = rule.getDeptColumn();
        String userColumn = rule.getUserColumn();
        boolean hasDeptColumn = StringUtils.hasText(deptColumn);
        boolean hasUserColumn = StringUtils.hasText(userColumn);

        Set<Long> deptIds = finalScope.getDeptIds();
        boolean needSelf = finalScope.isSelf();
        if (!hasDeptColumn && !deptIds.isEmpty()) {
            needSelf = true;
        }
        Expression deptExpr = null;
        if (hasDeptColumn && !deptIds.isEmpty()) {
            Column deptCol = toColumn(deptQualifier, deptColumn);
            deptExpr = buildDeptExpression(deptIds, deptCol);
        }
        Expression userExpr = null;
        if (needSelf && hasUserColumn && user.getId() != null) {
            Column userCol = toColumn(userQualifier, userColumn);
            userExpr = new EqualsTo(userCol, new LongValue(user.getId()));
        }
        if (deptExpr == null && userExpr == null) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        if (deptExpr != null && userExpr != null) {
            return new OrExpression(deptExpr, userExpr);
        }
        return deptExpr != null ? deptExpr : userExpr;
    }

    private Expression buildDeptExpression(Set<Long> deptIds, Column column) {
        if (deptIds == null || deptIds.isEmpty()) {
            return new EqualsTo(new LongValue(1), new LongValue(0));
        }
        if (deptIds.size() == 1) {
            Long only = deptIds.iterator().next();
            return new EqualsTo(column, new LongValue(only));
        }
        List<Expression> values = new ArrayList<>();
        for (Long id : deptIds) {
            if (id != null) {
                values.add(new LongValue(id));
            }
        }
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        in.setRightExpression(new ExpressionList<>(values));
        return in;
    }

    private Column toColumn(String qualifier, String columnName) {
        Column columnRef = new Column();
        columnRef.setColumnName(columnName);
        if (StringUtils.hasText(qualifier)) {
            columnRef.setTable(new Table(qualifier));
        }
        return columnRef;
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
     * 规则缺省值补全。
     */
    private EffectiveRule buildEffectiveRule(DataScopeRuleDefinition rule, DataScopeContextHolder.DataScopeRequest request) {
        EffectiveRule effective = new EffectiveRule();
        boolean hasRule = rule != null;
        if (rule != null) {
            effective.setTableName(rule.getTableName());
            effective.setTableAlias(rule.getTableAlias());
            effective.setDeptColumn(rule.getDeptColumn());
            effective.setUserColumn(rule.getUserColumn());
            effective.setFilterType(rule.getFilterType());
            effective.setStatus(rule.getStatus());
        }
        if (!hasRule) {
            if (!StringUtils.hasText(effective.getDeptColumn())) {
                effective.setDeptColumn(DEFAULT_DEPT_COLUMN);
            }
            if (!StringUtils.hasText(effective.getUserColumn())) {
                effective.setUserColumn(DEFAULT_USER_COLUMN);
            }
        } else {
            if (!StringUtils.hasText(effective.getDeptColumn())) {
                effective.setDeptColumn(null);
            }
            if (!StringUtils.hasText(effective.getUserColumn())) {
                effective.setUserColumn(null);
            }
        }
        if (effective.getFilterType() == null) {
            effective.setFilterType(1);
        }
        if (!StringUtils.hasText(effective.getTableAlias()) && request != null) {
            if (StringUtils.hasText(request.getDeptAlias())) {
                effective.setTableAlias(request.getDeptAlias());
            } else if (StringUtils.hasText(request.getUserAlias())) {
                effective.setTableAlias(request.getUserAlias());
            }
        }
        return effective;
    }

    private static final class EffectiveRule {
        @Getter
        private String tableName;
        @Getter
        private String tableAlias;
        @Getter
        private String deptColumn;
        @Getter
        private String userColumn;
        @Getter
        private Integer filterType;
        @Getter
        private Integer status;

        private void setTableName(String tableName) {
            this.tableName = tableName;
        }

        private void setTableAlias(String tableAlias) {
            this.tableAlias = tableAlias;
        }

        private void setDeptColumn(String deptColumn) {
            this.deptColumn = deptColumn;
        }

        private void setUserColumn(String userColumn) {
            this.userColumn = userColumn;
        }

        private void setFilterType(Integer filterType) {
            this.filterType = filterType;
        }

        private void setStatus(Integer status) {
            this.status = status;
        }
    }
}
