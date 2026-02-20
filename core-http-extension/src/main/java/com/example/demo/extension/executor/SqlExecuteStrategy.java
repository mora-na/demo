package com.example.demo.extension.executor;

import com.example.demo.common.mybatis.SqlGuardProperties;
import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApiTypeCodes;
import com.example.demo.extension.model.SqlExecuteConfig;
import com.example.demo.extension.support.DynamicApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 执行策略（仅支持 SELECT）。
 */
@Slf4j
@Component
public class SqlExecuteStrategy implements ExecuteStrategy {

    private static final Pattern SCHEMA_QUALIFIED_TABLE_PATTERN =
            Pattern.compile("(?i)\\b(?:from|join)\\s+\"?([a-z_][a-z0-9_]*)\"?\\.\"?[a-z_][a-z0-9_]*\"?\\b");

    private final DataSource dataSource;
    private final SqlGuardProperties sqlGuardProperties;
    private final DynamicApiConstants constants;
    private final Cache<DynamicApiExecutionContext, JdbcHolder> running;

    public SqlExecuteStrategy(DataSource dataSource,
                              SqlGuardProperties sqlGuardProperties,
                              DynamicApiConstants constants) {
        this.dataSource = dataSource;
        this.sqlGuardProperties = sqlGuardProperties;
        this.constants = constants;
        this.running = buildRunningCache();
    }

    @Override
    public String type() {
        return DynamicApiTypeCodes.SQL;
    }

    @Override
    public String displayName() {
        return "SQL";
    }

    @Override
    public Object parseConfig(String configJson, ObjectMapper objectMapper) throws Exception {
        if (StringUtils.isBlank(configJson)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        SqlExecuteConfig config = objectMapper.readValue(configJson, SqlExecuteConfig.class);
        if (config == null || StringUtils.isBlank(config.getSql())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid());
        }
        return config;
    }

    @Override
    public DynamicApiExecuteResult execute(DynamicApiExecutionContext context) {
        if (Thread.currentThread().isInterrupted()) {
            return DynamicApiExecuteResult.error(constants.getController().getServiceUnavailableCode(),
                    constants.getMessage().getTimeout(),
                    DynamicApiTerminationReason.TIMEOUT);
        }
        Object configObj = context.getConfig();
        if (!(configObj instanceof SqlExecuteConfig)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        SqlExecuteConfig config = (SqlExecuteConfig) configObj;
        String sql = StringUtils.trimToNull(config.getSql());
        if (sql == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        boolean guardEnabled = sqlGuardProperties != null && sqlGuardProperties.isEnabled();
        if (guardEnabled && sqlGuardProperties.isBlockMultiStatement() && hasMultipleStatements(sql)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        Select select = parseSelect(sql);
        if (select == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid(),
                    DynamicApiTerminationReason.ERROR);
        }
        if (guardEnabled && sqlGuardProperties.isBlockCrossSchemaJoin()) {
            if (!validateSchemaIsolation(sql, sqlGuardProperties.getAllowedSchemas())) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid(),
                        DynamicApiTerminationReason.ERROR);
            }
        }
        if (guardEnabled) {
            if (sqlGuardProperties.isBlockWithClause() && hasWithClause(select)) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid(),
                        DynamicApiTerminationReason.ERROR);
            }
            if (sqlGuardProperties.isBlockUnion() && hasUnion(select)) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid(),
                        DynamicApiTerminationReason.ERROR);
            }
            if (containsBlockedFunctions(sql, sqlGuardProperties.getBlockedFunctions())) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid(),
                        DynamicApiTerminationReason.ERROR);
            }
            if (!validateAllowedTables(select, sqlGuardProperties.getAllowedTables())) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid(),
                        DynamicApiTerminationReason.ERROR);
            }
            if (!validateAllowedColumns(select, sqlGuardProperties.getAllowedColumns())) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid(),
                        DynamicApiTerminationReason.ERROR);
            }
        }
        try {
            ParsedSql parsed = parseNamedParameters(sql, context.getRequest().getParams());
            List<Map<String, Object>> result = query(context, parsed, context.getTimeoutMs());
            return DynamicApiExecuteResult.success(result);
        } catch (DynamicApiException ex) {
            return DynamicApiExecuteResult.error(ex.getCode(), ex.getMessageKey(), DynamicApiTerminationReason.ERROR);
        } catch (Exception ex) {
            String traceId = MDC.get("traceId");
            log.error("Dynamic api sql execute failed: apiId={}, path={}, method={}, traceId={}",
                    context.getApiId(),
                    context.getPath(),
                    context.getMethod(),
                    traceId,
                    ex);
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed(),
                    DynamicApiTerminationReason.ERROR);
        }
    }

    @Override
    public void onTimeout(DynamicApiExecutionContext context) {
        cancelExecution(context);
    }

    @Override
    public void onError(DynamicApiExecutionContext context, Throwable error) {
        cancelExecution(context);
    }

    @Override
    public void onCancel(DynamicApiExecutionContext context, Throwable cause) {
        cancelExecution(context);
    }

    private List<Map<String, Object>> query(DynamicApiExecutionContext context, ParsedSql parsed, long timeoutMs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = null;
        JdbcHolder holder = null;
        int maxRows = Math.max(0, constants.getExecute().getSqlMaxRows());
        int fetchSize = constants.getExecute().getSqlFetchSize();
        try {
            statement = connection.prepareStatement(parsed.sql);
            holder = new JdbcHolder(connection, statement);
            running.put(context, holder);
            holder.timeoutMode = applyDatabaseTimeout(connection, timeoutMs);
            if (timeoutMs > 0) {
                statement.setQueryTimeout((int) Math.max(1, Math.ceil(timeoutMs / 1000.0)));
            }
            if (maxRows > 0) {
                statement.setMaxRows(maxRows + 1);
            }
            if (fetchSize > 0) {
                statement.setFetchSize(fetchSize);
            }
            if (parsed.parameters != null) {
                for (int i = 0; i < parsed.parameters.size(); i++) {
                    statement.setObject(i + 1, parsed.parameters.get(i));
                }
            }
            try (ResultSet rs = statement.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int count = meta.getColumnCount();
                while (rs.next()) {
                    if (maxRows > 0 && rows.size() >= maxRows) {
                        throw new DynamicApiException(constants.getController().getBadRequestCode(),
                                constants.getMessage().getResponseTooLarge());
                    }
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        String label = meta.getColumnLabel(i);
                        row.put(label, rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        } finally {
            running.invalidate(context);
            if (holder != null) {
                resetDatabaseTimeout(connection, holder.timeoutMode);
            } else {
                resetDatabaseTimeout(connection, DbTimeoutMode.NONE);
            }
            closeQuietly(statement);
            closeQuietly(connection);
        }
        return rows;
    }

    private DbTimeoutMode applyDatabaseTimeout(Connection connection, long timeoutMs) {
        if (connection == null || timeoutMs <= 0) {
            return DbTimeoutMode.NONE;
        }
        String product = null;
        try {
            product = connection.getMetaData() == null ? null : connection.getMetaData().getDatabaseProductName();
        } catch (Exception ex) {
            return DbTimeoutMode.NONE;
        }
        if (product == null) {
            return DbTimeoutMode.NONE;
        }
        String normalized = product.toLowerCase(Locale.ROOT);
        if (normalized.contains("postgresql")) {
            executeSessionSetting(connection, "SET statement_timeout = " + timeoutMs);
            return DbTimeoutMode.POSTGRES;
        }
        if (normalized.contains("mysql") || normalized.contains("mariadb")) {
            executeSessionSetting(connection, "SET SESSION MAX_EXECUTION_TIME=" + timeoutMs);
            return DbTimeoutMode.MYSQL;
        }
        return DbTimeoutMode.NONE;
    }

    private void executeSessionSetting(Connection connection, String sql) {
        if (connection == null || sql == null) {
            return;
        }
        java.sql.Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(sql);
        } catch (Exception ex) {
            log.debug("Dynamic api sql timeout setting skipped: {}", ex.getMessage());
        } finally {
            closeQuietly(statement);
        }
    }

    private void resetDatabaseTimeout(Connection connection, DbTimeoutMode mode) {
        if (connection == null || mode == null || mode == DbTimeoutMode.NONE) {
            return;
        }
        if (mode == DbTimeoutMode.POSTGRES) {
            executeSessionSetting(connection, "SET statement_timeout = 0");
            return;
        }
        if (mode == DbTimeoutMode.MYSQL) {
            executeSessionSetting(connection, "SET SESSION MAX_EXECUTION_TIME=0");
        }
    }

    private void cancelExecution(DynamicApiExecutionContext context) {
        if (context == null) {
            return;
        }
        JdbcHolder holder = running.getIfPresent(context);
        if (holder == null) {
            return;
        }
        running.invalidate(context);
        cancelExecution(holder);
    }

    private void cancelExecution(JdbcHolder holder) {
        if (holder == null) {
            return;
        }
        try {
            if (holder.statement != null) {
                holder.statement.cancel();
            }
        } catch (Exception ex) {
            log.debug("Dynamic api sql cancel failed: {}", ex.getMessage());
        } finally {
            resetDatabaseTimeout(holder.connection, holder.timeoutMode);
            closeQuietly(holder.statement);
            closeQuietly(holder.connection);
        }
    }

    private Cache<DynamicApiExecutionContext, JdbcHolder> buildRunningCache() {
        long expireMs = resolveRunningExpireMs();
        int maxEntries = resolveRunningMaxEntries();
        return Caffeine.newBuilder()
                .maximumSize(maxEntries)
                .expireAfterWrite(Duration.ofMillis(expireMs))
                .removalListener((DynamicApiExecutionContext context, JdbcHolder holder, RemovalCause cause) -> {
                    if (holder == null || cause == RemovalCause.EXPLICIT) {
                        return;
                    }
                    cancelExecution(holder);
                })
                .build();
    }

    private int resolveRunningMaxEntries() {
        int maxEntries = constants.getExecute().getRunningMaxEntries();
        return Math.max(1, maxEntries);
    }

    private long resolveRunningExpireMs() {
        long maxTimeoutMs = constants.getExecute().getMaxTimeoutMs();
        long defaultTimeoutMs = constants.getExecute().getDefaultTimeoutMs();
        long base = maxTimeoutMs > 0 ? maxTimeoutMs : Math.max(1000L, defaultTimeoutMs);
        long buffer = Math.max(0L, constants.getExecute().getRunningExpireBufferMs());
        return base + buffer;
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
            // ignore
        }
    }

    private Select parseSelect(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (statement instanceof Select) {
                return (Select) statement;
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean hasWithClause(Select select) {
        if (select == null) {
            return false;
        }
        List<WithItem> withItems = select.getWithItemsList();
        return withItems != null && !withItems.isEmpty();
    }

    private boolean hasUnion(Select select) {
        if (select == null) {
            return false;
        }
        return select.getSetOperationList() != null || select instanceof SetOperationList;
    }

    private boolean hasMultipleStatements(String sql) {
        int idx = sql.indexOf(';');
        if (idx < 0) {
            return false;
        }
        for (int i = idx + 1; i < sql.length(); i++) {
            if (!Character.isWhitespace(sql.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsBlockedFunctions(String sql, List<String> blockedFunctions) {
        if (sql == null || blockedFunctions == null || blockedFunctions.isEmpty()) {
            return false;
        }
        String lower = sql.toLowerCase(Locale.ROOT);
        for (String fn : blockedFunctions) {
            if (fn == null || fn.trim().isEmpty()) {
                continue;
            }
            String name = fn.trim().toLowerCase(Locale.ROOT);
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(name) + "\\s*\\(",
                    Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(lower).find()) {
                return true;
            }
        }
        return false;
    }

    private boolean validateAllowedTables(Select select, List<String> allowedTables) {
        if (allowedTables == null || allowedTables.isEmpty()) {
            return true;
        }
        if (select == null) {
            return false;
        }
        Set<String> allowed = new HashSet<>();
        for (String table : allowedTables) {
            if (table == null || table.trim().isEmpty()) {
                continue;
            }
            allowed.add(normalizeSqlIdentifier(table));
        }
        if (allowed.isEmpty()) {
            return true;
        }
        Set<String> tables = new TablesNamesFinder().getTables((Statement) select);
        if (tables == null || tables.isEmpty()) {
            return false;
        }
        for (String table : tables) {
            String normalized = normalizeSqlIdentifier(table);
            if (allowed.contains(normalized)) {
                continue;
            }
            String stripped = stripSchema(normalized);
            if (!allowed.contains(stripped)) {
                return false;
            }
        }
        return true;
    }

    private boolean validateAllowedColumns(Select select, List<String> allowedColumns) {
        if (allowedColumns == null || allowedColumns.isEmpty()) {
            return true;
        }
        if (select == null) {
            return false;
        }
        Set<String> allowed = new HashSet<>();
        for (String column : allowedColumns) {
            if (column == null || column.trim().isEmpty()) {
                continue;
            }
            allowed.add(normalizeSqlIdentifier(column));
        }
        if (allowed.isEmpty()) {
            return true;
        }
        PlainSelect plain = select.getPlainSelect();
        if (plain == null) {
            return false;
        }
        List<SelectItem<?>> items = plain.getSelectItems();
        if (items == null || items.isEmpty()) {
            return false;
        }
        for (SelectItem<?> item : items) {
            if (item == null || item.getExpression() == null) {
                return false;
            }
            if (item.getExpression() instanceof AllColumns || item.getExpression() instanceof AllTableColumns) {
                return false;
            }
            if (!(item.getExpression() instanceof net.sf.jsqlparser.schema.Column)) {
                return false;
            }
            net.sf.jsqlparser.schema.Column column =
                    (net.sf.jsqlparser.schema.Column) item.getExpression();
            String columnName = normalizeSqlIdentifier(column.getColumnName());
            String tableName = column.getTable() == null ? null : normalizeSqlIdentifier(column.getTable().getName());
            if (isAllowedColumn(allowed, tableName, columnName)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private boolean isAllowedColumn(Set<String> allowed, String tableName, String columnName) {
        if (columnName == null || columnName.isEmpty()) {
            return false;
        }
        if (allowed.contains(columnName)) {
            return true;
        }
        if (tableName != null && !tableName.isEmpty()) {
            return allowed.contains(tableName + "." + columnName);
        }
        return false;
    }

    private String normalizeSqlIdentifier(String raw) {
        if (raw == null) {
            return null;
        }
        String normalized = raw.trim();
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (normalized.startsWith("`") && normalized.endsWith("`") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String stripSchema(String table) {
        if (table == null) {
            return null;
        }
        int idx = table.lastIndexOf('.');
        if (idx < 0 || idx + 1 >= table.length()) {
            return table;
        }
        return table.substring(idx + 1);
    }

    private enum DbTimeoutMode {
        NONE,
        POSTGRES,
        MYSQL
    }

    private static class JdbcHolder {
        private final Connection connection;
        private final java.sql.Statement statement;
        private volatile DbTimeoutMode timeoutMode = DbTimeoutMode.NONE;

        private JdbcHolder(Connection connection, java.sql.Statement statement) {
            this.connection = connection;
            this.statement = statement;
        }
    }

    private boolean validateSchemaIsolation(String sql, List<String> allowedSchemas) {
        if (sql == null) {
            return true;
        }
        String lower = sql.toLowerCase(Locale.ROOT);
        if (!lower.contains(" join ")) {
            return true;
        }
        Matcher matcher = SCHEMA_QUALIFIED_TABLE_PATTERN.matcher(sql);
        Set<String> schemas = new HashSet<>();
        while (matcher.find()) {
            String schema = matcher.group(1);
            if (schema != null && !schema.trim().isEmpty()) {
                schemas.add(schema.trim().toLowerCase(Locale.ROOT));
            }
        }
        if (schemas.isEmpty()) {
            return true;
        }
        if (allowedSchemas != null && !allowedSchemas.isEmpty()) {
            Set<String> allowed = new HashSet<>();
            for (String schema : allowedSchemas) {
                if (schema != null && !schema.trim().isEmpty()) {
                    allowed.add(schema.trim().toLowerCase(Locale.ROOT));
                }
            }
            for (String schema : schemas) {
                if (!allowed.contains(schema)) {
                    return false;
                }
            }
        }
        return schemas.size() <= 1;
    }

    private ParsedSql parseNamedParameters(String sql, Map<String, Object> params) {
        if (params == null) {
            params = Collections.emptyMap();
        }
        StringBuilder builder = new StringBuilder();
        List<Object> values = new ArrayList<>();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                builder.append(c);
                continue;
            }
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                builder.append(c);
                continue;
            }
            if (!inSingleQuote && !inDoubleQuote && c == ':' && i + 1 < sql.length()) {
                char next = sql.charAt(i + 1);
                if (next == ':') {
                    builder.append(c);
                    continue;
                }
                if (Character.isLetter(next) || next == '_') {
                    int start = i + 1;
                    int end = start + 1;
                    while (end < sql.length()) {
                        char ch = sql.charAt(end);
                        if (Character.isLetterOrDigit(ch) || ch == '_') {
                            end++;
                        } else {
                            break;
                        }
                    }
                    String name = sql.substring(start, end);
                    if (!params.containsKey(name)) {
                        throw new IllegalArgumentException("Missing SQL param: " + name);
                    }
                    values.add(params.get(name));
                    builder.append('?');
                    i = end - 1;
                    continue;
                }
            }
            builder.append(c);
        }
        return new ParsedSql(builder.toString(), values);
    }

    private static class ParsedSql {
        private final String sql;
        private final List<Object> parameters;

        ParsedSql(String sql, List<Object> parameters) {
            this.sql = sql;
            this.parameters = parameters;
        }
    }
}
