package com.example.demo.common.mybatis;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 安全防护拦截器，用于阻断多语句执行与全表更新/删除。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class SqlGuardInnerInterceptor implements InnerInterceptor {

    private static final Pattern SCHEMA_QUALIFIED_TABLE_PATTERN =
            Pattern.compile("(?i)\\b(?:from|join)\\s+\"?([a-z_][a-z0-9_]*)\"?\\.\"?[a-z_][a-z0-9_]*\"?\\b");

    private final SqlGuardProperties properties;

    /**
     * 构建 SQL 防护拦截器。
     *
     * @param properties 防护配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public SqlGuardInnerInterceptor(SqlGuardProperties properties) {
        this.properties = properties;
    }

    /**
     * 在 SQL 预编译前执行防护判断。
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
        BoundSql boundSql = statementHandler.getBoundSql();
        if (boundSql == null) {
            return;
        }
        String sql = boundSql.getSql();
        if (sql == null) {
            return;
        }
        String trimmed = sql.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        if (properties.isBlockMultiStatement() && hasMultipleStatements(trimmed)) {
            throw new IllegalStateException("SQL guard blocked multiple statements");
        }
        if (properties.isBlockFullTable() && isUpdateOrDelete(trimmed) && !hasTopLevelWhere(trimmed)) {
            throw new IllegalStateException("SQL guard blocked full table update/delete");
        }
        if (properties.isBlockWithClause() && hasWithClause(trimmed)) {
            throw new IllegalStateException("SQL guard blocked WITH clause");
        }
        if (properties.isBlockUnion() && hasTopLevelKeyword(trimmed, "union")) {
            throw new IllegalStateException("SQL guard blocked UNION");
        }
        if (properties.isBlockCrossSchemaJoin()) {
            validateSchemaIsolation(trimmed);
        }
        if (requiresParseValidation()) {
            Statement statement = parseStatement(trimmed);
            validateParsedSql(statement);
        }
    }

    /**
     * 判断 SQL 是否为 UPDATE/DELETE。
     *
     * @param sql SQL 文本
     * @return true 表示 UPDATE/DELETE
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isUpdateOrDelete(String sql) {
        String stripped = stripLeadingComments(sql);
        String lower = stripped.toLowerCase(Locale.ROOT);
        return startsWithKeyword(lower, "update") || startsWithKeyword(lower, "delete");
    }

    private boolean hasWithClause(String sql) {
        String stripped = stripLeadingComments(sql);
        if (stripped.isEmpty()) {
            return false;
        }
        String lower = stripped.toLowerCase(Locale.ROOT);
        return startsWithKeyword(lower, "with");
    }

    private boolean hasTopLevelKeyword(String sql, String keyword) {
        String lower = sql.toLowerCase(Locale.ROOT);
        ScanState state = new ScanState(lower);
        while (state.hasNext()) {
            char c = state.next();
            if (state.isInLiteralOrComment()) {
                continue;
            }
            if (c == '(') {
                state.depth++;
                continue;
            }
            if (c == ')') {
                if (state.depth > 0) {
                    state.depth--;
                }
                continue;
            }
            if (state.depth == 0 && matchesKeywordAt(state.source, state.index - 1, keyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean requiresParseValidation() {
        return hasEntries(properties.getBlockedFunctions())
                || hasEntries(properties.getAllowedTables())
                || hasEntries(properties.getAllowedColumns());
    }

    private Statement parseStatement(String sql) {
        try {
            return CCJSqlParserUtil.parse(sql);
        } catch (Exception ex) {
            throw new IllegalStateException("SQL guard failed to parse SQL for validation", ex);
        }
    }

    private void validateParsedSql(Statement statement) {
        Set<String> blockedFunctions = normalizeList(properties.getBlockedFunctions());
        Set<String> allowedTables = normalizeList(properties.getAllowedTables());
        Set<String> allowedColumns = normalizeList(properties.getAllowedColumns());
        if (!allowedTables.isEmpty()) {
            validateAllowedTables(statement, allowedTables);
        }
        if (!allowedColumns.isEmpty() || !blockedFunctions.isEmpty()) {
            SqlGuardExpressionChecker checker = new SqlGuardExpressionChecker(allowedColumns, blockedFunctions);
            checker.check(statement);
        }
    }

    private void validateSchemaIsolation(String sql) {
        String lower = stripLeadingComments(sql).toLowerCase(Locale.ROOT);
        if (!lower.contains(" join ")) {
            return;
        }
        Matcher matcher = SCHEMA_QUALIFIED_TABLE_PATTERN.matcher(sql);
        Set<String> schemas = new HashSet<>();
        while (matcher.find()) {
            String schema = matcher.group(1);
            if (schema == null || schema.trim().isEmpty()) {
                continue;
            }
            schemas.add(schema.trim().toLowerCase(Locale.ROOT));
        }
        if (schemas.isEmpty()) {
            return;
        }
        List<String> allowedSchemas = properties.getAllowedSchemas();
        if (allowedSchemas != null && !allowedSchemas.isEmpty()) {
            Set<String> allowed = new HashSet<>();
            for (String schema : allowedSchemas) {
                if (schema != null && !schema.trim().isEmpty()) {
                    allowed.add(schema.trim().toLowerCase(Locale.ROOT));
                }
            }
            for (String schema : schemas) {
                if (!allowed.contains(schema)) {
                    throw new IllegalStateException("SQL guard blocked non-whitelisted schema usage: " + schema);
                }
            }
        }
        if (schemas.size() > 1) {
            throw new IllegalStateException("SQL guard blocked cross-schema join");
        }
    }

    private void validateAllowedTables(Statement statement, Set<String> allowedTables) {
        if (statement == null || allowedTables == null || allowedTables.isEmpty()) {
            return;
        }
        TablesNamesFinder finder = new TablesNamesFinder();
        List<String> tables = finder.getTableList(statement);
        if (tables == null || tables.isEmpty()) {
            return;
        }
        for (String table : tables) {
            if (!isAllowedTable(table, allowedTables)) {
                throw new IllegalStateException("SQL guard blocked non-whitelisted table: " + table);
            }
        }
    }

    private boolean isAllowedTable(String table, Set<String> allowedTables) {
        String normalized = normalizeIdentifier(table);
        if (normalized == null || normalized.isEmpty()) {
            return true;
        }
        if (allowedTables.contains(normalized)) {
            return true;
        }
        int dot = normalized.indexOf('.');
        if (dot > 0) {
            String tableOnly = normalized.substring(dot + 1);
            return allowedTables.contains(tableOnly);
        }
        return false;
    }

    private boolean hasEntries(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private Set<String> normalizeList(List<String> values) {
        Set<String> result = new HashSet<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            String normalized = normalizeIdentifier(value);
            if (normalized != null && !normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeIdentifier(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        normalized = normalized.replace("`", "")
                .replace("\"", "")
                .replace("[", "")
                .replace("]", "")
                .replace(" ", "");
        return normalized;
    }

    /**
     * 判断 SQL 顶层是否存在多个语句。
     *
     * @param sql SQL 文本
     * @return true 表示包含多个语句
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean hasMultipleStatements(String sql) {
        int semicolonIndex = findTopLevelSemicolon(sql);
        if (semicolonIndex < 0) {
            return false;
        }
        for (int i = semicolonIndex + 1; i < sql.length(); i++) {
            if (!Character.isWhitespace(sql.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找顶层分号位置，忽略字符串、注释、括号嵌套。
     *
     * @param sql SQL 文本
     * @return 分号索引，未找到返回 -1
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int findTopLevelSemicolon(String sql) {
        ScanState state = new ScanState(sql);
        while (state.hasNext()) {
            char c = state.next();
            if (state.isInLiteralOrComment()) {
                continue;
            }
            if (c == '(') {
                state.depth++;
                continue;
            }
            if (c == ')') {
                if (state.depth > 0) {
                    state.depth--;
                }
                continue;
            }
            if (state.depth == 0 && c == ';') {
                return state.index - 1;
            }
        }
        return -1;
    }

    /**
     * 判断 SQL 顶层是否包含 WHERE 子句。
     *
     * @param sql SQL 文本
     * @return true 表示存在顶层 WHERE
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean hasTopLevelWhere(String sql) {
        ScanState state = new ScanState(sql.toLowerCase(Locale.ROOT));
        while (state.hasNext()) {
            char c = state.next();
            if (state.isInLiteralOrComment()) {
                continue;
            }
            if (c == '(') {
                state.depth++;
                continue;
            }
            if (c == ')') {
                if (state.depth > 0) {
                    state.depth--;
                }
                continue;
            }
            if (state.depth == 0 && matchesKeywordAt(state.source, state.index - 1, "where")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移除 SQL 前置注释，便于关键字判断。
     *
     * @param sql SQL 文本
     * @return 去除前置注释后的 SQL
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String stripLeadingComments(String sql) {
        int i = 0;
        int length = sql.length();
        while (i < length) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '-' && i + 1 < length && sql.charAt(i + 1) == '-') {
                i = skipLine(sql, i + 2);
                continue;
            }
            if (c == '/' && i + 1 < length && sql.charAt(i + 1) == '*') {
                i = skipBlock(sql, i + 2);
                continue;
            }
            break;
        }
        return sql.substring(i);
    }

    /**
     * 跳过单行注释。
     *
     * @param sql   SQL 文本
     * @param start 起始索引
     * @return 跳过后的索引
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int skipLine(String sql, int start) {
        int i = start;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\n' || c == '\r') {
                return i + 1;
            }
            i++;
        }
        return i;
    }

    /**
     * 跳过块注释。
     *
     * @param sql   SQL 文本
     * @param start 起始索引
     * @return 跳过后的索引
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private int skipBlock(String sql, int start) {
        int i = start;
        while (i + 1 < sql.length()) {
            if (sql.charAt(i) == '*' && sql.charAt(i + 1) == '/') {
                return i + 2;
            }
            i++;
        }
        return sql.length();
    }

    /**
     * 判断字符串是否以关键字起始，并校验边界字符。
     *
     * @param sql     SQL 文本（已小写）
     * @param keyword 关键字
     * @return true 表示匹配关键字
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean startsWithKeyword(String sql, String keyword) {
        if (!sql.startsWith(keyword)) {
            return false;
        }
        if (sql.length() == keyword.length()) {
            return true;
        }
        return !isIdentifierChar(sql.charAt(keyword.length()));
    }

    /**
     * 在指定位置判断是否匹配关键字（含边界判断）。
     *
     * @param sql     SQL 文本（已小写）
     * @param index   关键字起始索引
     * @param keyword 关键字
     * @return true 表示匹配
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean matchesKeywordAt(String sql, int index, String keyword) {
        if (index < 0 || index + keyword.length() > sql.length()) {
            return false;
        }
        for (int i = 0; i < keyword.length(); i++) {
            if (sql.charAt(index + i) != keyword.charAt(i)) {
                return false;
            }
        }
        int before = index - 1;
        int after = index + keyword.length();
        if (before >= 0 && isIdentifierChar(sql.charAt(before))) {
            return false;
        }
        return after >= sql.length() || !isIdentifierChar(sql.charAt(after));
    }

    /**
     * 判断字符是否为 SQL 标识符组成字符。
     *
     * @param c 字符
     * @return true 表示可作为标识符
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private final class SqlGuardExpressionChecker extends StatementVisitorAdapter {
        private final Set<String> allowedColumns;
        private final Set<String> blockedFunctions;
        private final SelectVisitorAdapter selectVisitor;
        private final FromItemVisitorAdapter fromItemVisitor;
        private final ExpressionVisitorAdapter expressionVisitor;

        private SqlGuardExpressionChecker(Set<String> allowedColumns, Set<String> blockedFunctions) {
            this.allowedColumns = allowedColumns == null ? new HashSet<>() : allowedColumns;
            this.blockedFunctions = blockedFunctions == null ? new HashSet<>() : blockedFunctions;
            this.selectVisitor = new SelectVisitorAdapter() {
                @Override
                public void visit(PlainSelect plainSelect) {
                    if (plainSelect == null) {
                        return;
                    }
                    List<SelectItem<?>> items = plainSelect.getSelectItems();
                    if (items != null) {
                        for (SelectItem<?> item : items) {
                            if (item != null) {
                                item.accept(expressionVisitor);
                            }
                        }
                    }
                    visitExpression(plainSelect.getWhere());
                    visitExpression(plainSelect.getHaving());
                    visitExpression(plainSelect.getQualify());
                    if (plainSelect.getGroupBy() != null && plainSelect.getGroupBy().getGroupByExpressions() != null) {
                        ExpressionList<?> groupBy = plainSelect.getGroupBy().getGroupByExpressions();
                        if (groupBy.getExpressions() != null) {
                            for (Object expression : groupBy.getExpressions()) {
                                if (expression instanceof Expression) {
                                    visitExpression((Expression) expression);
                                }
                            }
                        }
                    }
                    if (plainSelect.getOrderByElements() != null) {
                        for (OrderByElement element : plainSelect.getOrderByElements()) {
                            if (element != null) {
                                visitExpression(element.getExpression());
                            }
                        }
                    }
                    FromItem fromItem = plainSelect.getFromItem();
                    if (fromItem != null) {
                        fromItem.accept(fromItemVisitor);
                    }
                    if (plainSelect.getJoins() != null) {
                        for (Join join : plainSelect.getJoins()) {
                            handleJoin(join);
                        }
                    }
                }

                @Override
                public void visit(SetOperationList setOperationList) {
                    if (setOperationList == null || setOperationList.getSelects() == null) {
                        return;
                    }
                    for (Select select : setOperationList.getSelects()) {
                        if (select != null) {
                            select.accept(this);
                        }
                    }
                }

                @Override
                public void visit(ParenthesedSelect parenthesedSelect) {
                    if (parenthesedSelect == null) {
                        return;
                    }
                    Select select = parenthesedSelect.getSelect();
                    if (select != null) {
                        select.accept(this);
                    }
                }

                @Override
                public void visit(WithItem withItem) {
                    if (withItem == null) {
                        return;
                    }
                    Select select = withItem.getSelect();
                    if (select != null) {
                        select.accept(this);
                    }
                }

                @Override
                public void visit(Values values) {
                    if (values == null || values.getExpressions() == null) {
                        return;
                    }
                    ExpressionList<?> expressions = values.getExpressions();
                    if (expressions.getExpressions() != null) {
                        for (Object expr : expressions.getExpressions()) {
                            if (expr instanceof Expression) {
                                visitExpression((Expression) expr);
                            }
                        }
                    }
                }
            };
            this.fromItemVisitor = new FromItemVisitorAdapter() {
                @Override
                public void visit(ParenthesedSelect parenthesedSelect) {
                    if (parenthesedSelect == null) {
                        return;
                    }
                    Select select = parenthesedSelect.getSelect();
                    if (select != null) {
                        select.accept(selectVisitor);
                    }
                }
            };
            this.expressionVisitor = new ExpressionVisitorAdapter() {
                @Override
                public void visit(Column column) {
                    checkColumn(column);
                }

                @Override
                public void visit(AllColumns allColumns) {
                    checkWildcard(null);
                }

                @Override
                public void visit(AllTableColumns allTableColumns) {
                    Table table = allTableColumns == null ? null : allTableColumns.getTable();
                    checkWildcard(table);
                }

                @Override
                public void visit(Function function) {
                    checkFunction(function);
                    super.visit(function);
                }
            };
            this.expressionVisitor.setSelectVisitor(selectVisitor);
        }

        private void check(Statement statement) {
            if (statement == null) {
                return;
            }
            statement.accept(this);
        }

        @Override
        public void visit(Select select) {
            if (select == null) {
                return;
            }
            if (select.getWithItemsList() != null) {
                for (WithItem withItem : select.getWithItemsList()) {
                    if (withItem != null) {
                        selectVisitor.visit(withItem);
                    }
                }
            }
            Select body = select.getSelectBody();
            if (body != null) {
                body.accept(selectVisitor);
            }
        }

        @Override
        public void visit(Update update) {
            if (update == null) {
                return;
            }
            if (update.getColumns() != null) {
                for (Column column : update.getColumns()) {
                    checkColumn(column);
                }
            }
            if (update.getExpressions() != null) {
                for (Expression expression : update.getExpressions()) {
                    visitExpression(expression);
                }
            }
            visitExpression(update.getWhere());
            if (update.getFromItem() != null) {
                update.getFromItem().accept(fromItemVisitor);
            }
            if (update.getJoins() != null) {
                for (Join join : update.getJoins()) {
                    handleJoin(join);
                }
            }
            if (update.getSelect() != null) {
                update.getSelect().accept(selectVisitor);
            }
        }

        @Override
        public void visit(Delete delete) {
            if (delete == null) {
                return;
            }
            visitExpression(delete.getWhere());
            if (delete.getJoins() != null) {
                for (Join join : delete.getJoins()) {
                    handleJoin(join);
                }
            }
        }

        @Override
        public void visit(Insert insert) {
            if (insert == null) {
                return;
            }
            if (insert.getColumns() != null && insert.getColumns().getExpressions() != null) {
                for (Object column : insert.getColumns().getExpressions()) {
                    if (column instanceof Column) {
                        checkColumn((Column) column);
                    }
                }
            }
            if (insert.getSelect() != null) {
                insert.getSelect().accept(selectVisitor);
            } else if (insert.getValues() != null) {
                selectVisitor.visit(insert.getValues());
            }
        }

        private void handleJoin(Join join) {
            if (join == null) {
                return;
            }
            if (join.getRightItem() != null) {
                join.getRightItem().accept(fromItemVisitor);
            }
            if (join.getOnExpression() != null) {
                visitExpression(join.getOnExpression());
            }
            if (join.getOnExpressions() != null) {
                for (Expression expression : join.getOnExpressions()) {
                    visitExpression(expression);
                }
            }
            if (join.getUsingColumns() != null) {
                for (Column column : join.getUsingColumns()) {
                    checkColumn(column);
                }
            }
        }

        private void visitExpression(Expression expression) {
            if (expression != null) {
                expression.accept(expressionVisitor);
            }
        }

        private void checkFunction(Function function) {
            if (function == null || blockedFunctions.isEmpty()) {
                return;
            }
            String name = normalizeIdentifier(function.getName());
            if (name != null && blockedFunctions.contains(name)) {
                throw new IllegalStateException("SQL guard blocked function: " + function.getName());
            }
        }

        private void checkColumn(Column column) {
            if (column == null || allowedColumns.isEmpty()) {
                return;
            }
            String columnName = normalizeIdentifier(column.getColumnName());
            if (columnName == null || columnName.isEmpty()) {
                return;
            }
            if (allowedColumns.contains(columnName)) {
                return;
            }
            String tableName = normalizeTableName(column.getTable());
            if (tableName != null && !tableName.isEmpty()) {
                if (allowedColumns.contains(tableName + "." + columnName)) {
                    return;
                }
                int dot = tableName.indexOf('.');
                if (dot > 0) {
                    String simpleTable = tableName.substring(dot + 1);
                    if (allowedColumns.contains(simpleTable + "." + columnName)) {
                        return;
                    }
                }
            }
            throw new IllegalStateException("SQL guard blocked non-whitelisted column: " + column.getFullyQualifiedName());
        }

        private void checkWildcard(Table table) {
            if (allowedColumns.isEmpty()) {
                return;
            }
            if (allowedColumns.contains("*")) {
                return;
            }
            String tableName = normalizeTableName(table);
            if (tableName != null && !tableName.isEmpty()) {
                if (allowedColumns.contains(tableName + ".*")) {
                    return;
                }
                int dot = tableName.indexOf('.');
                if (dot > 0) {
                    String simpleTable = tableName.substring(dot + 1);
                    if (allowedColumns.contains(simpleTable + ".*")) {
                        return;
                    }
                }
            }
            throw new IllegalStateException("SQL guard blocked wildcard column usage");
        }

        private String normalizeTableName(Table table) {
            if (table == null) {
                return null;
            }
            return normalizeIdentifier(table.getFullyQualifiedName());
        }
    }

    /**
     * SQL 扫描状态机，用于跳过字面量/注释并跟踪括号层级。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static final class ScanState {
        private final String source;
        private int index;
        private int depth;
        private boolean inSingleQuote;
        private boolean inDoubleQuote;
        private boolean inBacktick;
        private boolean inLineComment;
        private boolean inBlockComment;

        /**
         * 构建扫描状态。
         *
         * @param source SQL 文本
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private ScanState(String source) {
            this.source = source;
        }

        /**
         * 是否还有可扫描字符。
         *
         * @return true 表示未扫描完
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private boolean hasNext() {
            return index < source.length();
        }

        /**
         * 读取下一个字符并更新状态机。
         *
         * @return 当前字符
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private char next() {
            char c = source.charAt(index++);
            if (inLineComment) {
                if (c == '\n' || c == '\r') {
                    inLineComment = false;
                }
                return c;
            }
            if (inBlockComment) {
                if (c == '*' && index < source.length() && source.charAt(index) == '/') {
                    inBlockComment = false;
                    index++;
                }
                return c;
            }
            if (inSingleQuote) {
                if (c == '\'') {
                    if (index < source.length() && source.charAt(index) == '\'') {
                        index++;
                    } else {
                        inSingleQuote = false;
                    }
                }
                return c;
            }
            if (inDoubleQuote) {
                if (c == '"') {
                    if (index < source.length() && source.charAt(index) == '"') {
                        index++;
                    } else {
                        inDoubleQuote = false;
                    }
                }
                return c;
            }
            if (inBacktick) {
                if (c == '`') {
                    inBacktick = false;
                }
                return c;
            }
            if (c == '-' && index < source.length() && source.charAt(index) == '-') {
                inLineComment = true;
                index++;
                return c;
            }
            if (c == '/' && index < source.length() && source.charAt(index) == '*') {
                inBlockComment = true;
                index++;
                return c;
            }
            if (c == '\'') {
                inSingleQuote = true;
            } else if (c == '"') {
                inDoubleQuote = true;
            } else if (c == '`') {
                inBacktick = true;
            }
            return c;
        }

        /**
         * 判断是否处于字面量或注释中。
         *
         * @return true 表示处于字面量或注释
         * @author GPT-5.2-codex(high)
         * @date 2026/2/9
         */
        private boolean isInLiteralOrComment() {
            return inSingleQuote || inDoubleQuote || inBacktick || inLineComment || inBlockComment;
        }
    }
}
