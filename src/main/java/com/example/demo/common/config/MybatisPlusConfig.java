package com.example.demo.common.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.example.demo.common.mybatis.*;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
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
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        if (properties.isEnabled()) {
            interceptor.addInnerInterceptor(new SqlGuardInnerInterceptor(properties));
        }
        if (dataScopeProperties.isEnabled()) {
            interceptor.addInnerInterceptor(new DataScopeInnerInterceptor(dataScopeProperties, dataScopeRuleProvider));
        }
        return interceptor;
    }

    /**
     * 根据配置开关控制 MyBatis SQL 日志输出。
     *
     * @param sqlLogProperties SQL 日志配置
     * @return 配置自定义器
     * @author GPT-5.2-codex(high)
     * @date 2026/2/11
     */
    @Bean
    public ConfigurationCustomizer mybatisConfigurationCustomizer(SqlLogProperties sqlLogProperties) {
        return configuration -> {
            if (sqlLogProperties.isEnabled()) {
                configuration.setLogImpl(Slf4jImpl.class);
            } else {
                configuration.setLogImpl(NoLoggingImpl.class);
            }
        };
    }
}
