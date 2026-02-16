package com.example.demo.config;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 单 schema 模式 SQL 重写拦截器注册。
 */
@Configuration
@ConditionalOnProperty(prefix = "app.datasource.sql-rewrite", name = "enabled", havingValue = "true")
public class SingleSchemaSqlRewriteConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Interceptor singleSchemaSqlRewriteInterceptor(
            @Value("${app.datasource.sql-rewrite.target-schema:demo}") String targetSchema
    ) {
        return new SingleSchemaSqlRewriteInterceptor(targetSchema);
    }
}
