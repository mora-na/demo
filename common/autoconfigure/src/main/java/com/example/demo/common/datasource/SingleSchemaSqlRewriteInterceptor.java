package com.example.demo.common.datasource;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 单 schema 模式下，将 SQL 中硬编码的模块 schema 前缀统一重写为目标 schema。
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class SingleSchemaSqlRewriteInterceptor implements Interceptor {

    private final String targetSchema;
    private final String replacementPrefix;
    private final Set<String> sourceSchemas;
    private final boolean fallbackToRegex;
    private final Pattern fallbackPattern;
    private final Map<String, String> rewriteCache;

    public SingleSchemaSqlRewriteInterceptor(SingleSchemaSqlRewriteProperties properties) {
        String target = properties == null ? null : properties.getTargetSchema();
        if (target == null || !target.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid target schema for single-schema mode: " + target);
        }
        this.targetSchema = target;
        this.replacementPrefix = target + ".";
        this.sourceSchemas = normalizeSchemas(properties == null ? null : properties.getSourceSchemas());
        this.fallbackToRegex = properties != null && properties.isFallbackToRegex();
        this.fallbackPattern = fallbackToRegex ? buildFallbackPattern(sourceSchemas) : null;
        int cacheSize = properties == null ? 0 : properties.getCacheSize();
        this.rewriteCache = cacheSize > 0
                ? Collections.synchronizedMap(new LinkedHashMap<String, String>(cacheSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > cacheSize;
            }
        })
                : null;
    }

    private static Set<String> normalizeSchemas(Iterable<String> schemas) {
        if (schemas == null) {
            return Collections.emptySet();
        }
        Set<String> normalized = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (String schema : schemas) {
            if (schema == null) {
                continue;
            }
            String trimmed = schema.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed.toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }

    private static Pattern buildFallbackPattern(Set<String> schemas) {
        if (schemas == null || schemas.isEmpty()) {
            return Pattern.compile("a^");
        }
        String joined = schemas.stream()
                .sorted()
                .map(SingleSchemaSqlRewriteInterceptor::schemaTokenPattern)
                .collect(Collectors.joining("|"));
        String regex = "(?i)(?<![a-z0-9_`\"])" + "(?:" + joined + ")\\s*\\.";
        return Pattern.compile(regex);
    }

    private static String schemaTokenPattern(String schema) {
        String escaped = Pattern.quote(schema);
        return "\"" + escaped + "\"|`" + escaped + "`|" + escaped;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) unwrapProxy(invocation.getTarget());
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();
        if (originalSql == null || originalSql.isEmpty()) {
            return invocation.proceed();
        }
        String rewrittenSql = rewriteSql(originalSql);
        if (!originalSql.equals(rewrittenSql)) {
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql", rewrittenSql);
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // no-op
    }

    private String rewriteSql(String sql) {
        if (sourceSchemas.isEmpty()) {
            return sql;
        }
        if (rewriteCache != null) {
            String cached = rewriteCache.get(sql);
            if (cached != null) {
                return cached;
            }
        }
        String rewritten = rewriteWithParser(sql);
        if (rewritten == null && fallbackToRegex && fallbackPattern != null) {
            rewritten = fallbackPattern.matcher(sql).replaceAll(replacementPrefix);
        }
        if (rewritten == null) {
            rewritten = sql;
        }
        if (rewriteCache != null) {
            rewriteCache.put(sql, rewritten);
        }
        return rewritten;
    }

    private String rewriteWithParser(String sql) {
        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sql);
            for (Statement statement : statements.getStatements()) {
                statement.accept(new SchemaRewriteVisitor(sourceSchemas, targetSchema));
            }
            return statements.toString();
        } catch (JSQLParserException ex) {
            return null;
        }
    }

    private Object unwrapProxy(Object target) {
        MetaObject metaObject = SystemMetaObject.forObject(target);
        while (metaObject.hasGetter("h")) {
            target = metaObject.getValue("h.target");
            metaObject = SystemMetaObject.forObject(target);
        }
        while (metaObject.hasGetter("target")) {
            target = metaObject.getValue("target");
            metaObject = SystemMetaObject.forObject(target);
        }
        return target;
    }

    private static class SchemaRewriteVisitor extends TablesNamesFinder {

        private final Set<String> sourceSchemas;
        private final String targetSchema;

        private SchemaRewriteVisitor(Set<String> sourceSchemas, String targetSchema) {
            this.sourceSchemas = sourceSchemas;
            this.targetSchema = targetSchema;
        }

        @Override
        public void visit(Table tableName) {
            String schemaName = normalizeIdentifier(tableName.getSchemaName());
            if (schemaName != null && sourceSchemas.contains(schemaName.toLowerCase(Locale.ROOT))) {
                tableName.setSchemaName(targetSchema);
            }
            super.visit(tableName);
        }

        private String normalizeIdentifier(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                    || (trimmed.startsWith("`") && trimmed.endsWith("`"))) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            return trimmed;
        }
    }
}
