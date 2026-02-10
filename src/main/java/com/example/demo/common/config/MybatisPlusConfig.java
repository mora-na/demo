package com.example.demo.common.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.example.demo.common.mybatis.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置，注入拦截器链。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器链，按配置启用 SQL 防护与数据范围拦截。
     *
     * @param properties            SQL 防护配置
     * @param dataScopeProperties   数据范围配置
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
