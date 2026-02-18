package com.example.demo.extension.executor;

import com.example.demo.common.mybatis.SqlGuardProperties;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.DynamicApiTypeCodes;
import com.example.demo.extension.model.SqlExecuteConfig;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.example.demo.extension.support.DynamicApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
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

    public SqlExecuteStrategy(DataSource dataSource,
                              SqlGuardProperties sqlGuardProperties,
                              DynamicApiConstants constants) {
        this.dataSource = dataSource;
        this.sqlGuardProperties = sqlGuardProperties;
        this.constants = constants;
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
    public DynamicApiExecuteResult execute(DynamicApiContext context) {
        DynamicApiMeta meta = context.getMeta();
        Object configObj = meta.getConfig();
        if (!(configObj instanceof SqlExecuteConfig)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid());
        }
        SqlExecuteConfig config = (SqlExecuteConfig) configObj;
        String sql = StringUtils.trimToNull(config.getSql());
        if (sql == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid());
        }
        if (sqlGuardProperties != null && sqlGuardProperties.isBlockMultiStatement() && hasMultipleStatements(sql)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid());
        }
        if (!isSelectOnly(sql)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getSqlInvalid());
        }
        if (sqlGuardProperties != null && sqlGuardProperties.isBlockCrossSchemaJoin()) {
            if (!validateSchemaIsolation(sql, sqlGuardProperties.getAllowedSchemas())) {
                return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                        constants.getMessage().getSqlInvalid());
            }
        }
        try {
            ParsedSql parsed = parseNamedParameters(sql, context.getRequest().getParams());
            List<Map<String, Object>> result = query(parsed, context.getTimeoutMs());
            return DynamicApiExecuteResult.success(result);
        } catch (Exception ex) {
            String traceId = MDC.get("traceId");
            log.error("Dynamic api sql execute failed: apiId={}, path={}, method={}, traceId={}",
                    context.getMeta().getApi().getId(),
                    context.getMeta().getApi().getPath(),
                    context.getMeta().getApi().getMethod(),
                    traceId,
                    ex);
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed());
        }
    }

    private List<Map<String, Object>> query(ParsedSql parsed, long timeoutMs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(parsed.sql)) {
            if (timeoutMs > 0) {
                statement.setQueryTimeout((int) Math.max(1, Math.ceil(timeoutMs / 1000.0)));
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
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        String label = meta.getColumnLabel(i);
                        row.put(label, rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private boolean isSelectOnly(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            return statement instanceof Select;
        } catch (Exception ex) {
            return false;
        }
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
