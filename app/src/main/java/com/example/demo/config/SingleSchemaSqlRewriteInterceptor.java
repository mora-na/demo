package com.example.demo.config;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * 单 schema 模式下，将 SQL 中硬编码的模块 schema 前缀统一重写为目标 schema。
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})
})
public class SingleSchemaSqlRewriteInterceptor implements Interceptor {

    private static final Pattern SCHEMA_PREFIX_PATTERN = Pattern.compile(
            "(?i)(?<![a-z0-9_`\"])(system|notice|job|log|dict|cache|\"order\"|`order`|order)\\s*\\."
    );

    private final String replacementPrefix;

    public SingleSchemaSqlRewriteInterceptor(String targetSchema) {
        if (targetSchema == null || !targetSchema.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Invalid target schema for single-schema mode: " + targetSchema);
        }
        this.replacementPrefix = targetSchema + ".";
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) unwrapProxy(invocation.getTarget());
        BoundSql boundSql = statementHandler.getBoundSql();
        String originalSql = boundSql.getSql();
        String rewrittenSql = SCHEMA_PREFIX_PATTERN.matcher(originalSql).replaceAll(replacementPrefix);
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
}
