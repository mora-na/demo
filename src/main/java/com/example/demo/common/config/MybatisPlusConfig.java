package com.example.demo.common.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.example.demo.common.mybatis.*;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置，注入自定义 SQL 注入器与拦截器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册自定义 SQL 注入器。
     *
     * @return SQL 注入器实例
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Bean
    public ISqlInjector mppSqlInjector() {
        return new MppSqlInjector();
    }

    /**
     * 注册 MyBatis-Plus 拦截器链，按配置启用 SQL 防护与数据范围拦截。
     *
     * @param properties           SQL 防护配置
     * @param dataScopeProperties  数据范围配置
     * @param dataScopeRuleProvider 数据范围规则提供者
     * @return MyBatis-Plus 拦截器
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(SqlGuardProperties properties,
                                                         DataScopeProperties dataScopeProperties,
                                                         DataScopeRuleProvider dataScopeRuleProvider) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        if (properties.isEnabled()) {
            interceptor.addInnerInterceptor(new SqlGuardInnerInterceptor(properties));
        }
        if (dataScopeProperties.isEnabled()) {
            interceptor.addInnerInterceptor(new DataScopeInnerInterceptor(dataScopeProperties, dataScopeRuleProvider));
        }
        return interceptor;
    }
}
