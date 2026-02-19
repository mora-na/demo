package com.example.demo.common.datasource;

import org.apache.ibatis.plugin.Interceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 单 schema 模式 SQL 重写拦截器注册。
 */
@Configuration
@ConditionalOnProperty(prefix = "app.datasource.sql-rewrite", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SingleSchemaSqlRewriteProperties.class)
public class SingleSchemaSqlRewriteConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Interceptor singleSchemaSqlRewriteInterceptor(SingleSchemaSqlRewriteProperties properties) {
        return new SingleSchemaSqlRewriteInterceptor(properties);
    }
}
