package com.example.demo.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.example.demo.common.mybatis.*;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Locale;

/**
 * MyBatis-Plus 配置，注入拦截器链。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
public class MybatisPlusConfig {

    private final Environment environment;
    private final CommonConstants systemConstants;

    public MybatisPlusConfig(Environment environment, CommonConstants systemConstants) {
        this.environment = environment;
        this.systemConstants = systemConstants;
    }

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
                                                         DataScopeRuleProvider dataScopeRuleProvider,
                                                         com.example.demo.datascope.service.DataScopeEvaluator evaluator) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        if (dataScopeProperties.isEnabled()) {
            interceptor.addInnerInterceptor(new DataScopeInnerInterceptor(dataScopeProperties, dataScopeRuleProvider, evaluator));
        }
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        if (properties.isEnabled()) {
            interceptor.addInnerInterceptor(new SqlGuardInnerInterceptor(properties));
        }
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    private PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor interceptor = new PaginationInnerInterceptor();
        DbType dbType = resolveDbType();
        if (dbType != null) {
            interceptor.setDbType(dbType);
        }
        return interceptor;
    }

    private DbType resolveDbType() {
        String url = environment == null ? null : environment.getProperty(systemConstants.getMybatis().getDatasourceUrlProperty());
        if (url == null) {
            return null;
        }
        String normalized = url.toLowerCase(Locale.ROOT);
        if (normalized.contains(systemConstants.getMybatis().getPostgresToken())) {
            return DbType.POSTGRE_SQL;
        }
        if (normalized.contains(systemConstants.getMybatis().getMysqlToken())) {
            return DbType.MYSQL;
        }
        if (normalized.contains(systemConstants.getMybatis().getMariadbToken())) {
            return DbType.MARIADB;
        }
        if (normalized.contains(systemConstants.getMybatis().getOracleToken())) {
            return DbType.ORACLE;
        }
        return null;
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
