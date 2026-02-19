package com.example.demo.common.datasource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模块数据源路由配置入口。
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(ModuleDataSourceRoutingProperties.class)
public class ModuleDataSourceRoutingConfiguration {

    @Bean
    public ModuleDataSourceRoutingAspect moduleDataSourceRoutingAspect(ModuleDataSourceRoutingProperties properties) {
        return new ModuleDataSourceRoutingAspect(properties);
    }
}
