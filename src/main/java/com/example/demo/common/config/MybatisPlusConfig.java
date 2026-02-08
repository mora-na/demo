package com.example.demo.common.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.example.demo.common.mybatis.SqlGuardInnerInterceptor;
import com.example.demo.common.mybatis.SqlGuardProperties;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public ISqlInjector mppSqlInjector() {
        return new MppSqlInjector();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(SqlGuardProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        if (properties.isEnabled()) {
            interceptor.addInnerInterceptor(new SqlGuardInnerInterceptor(properties));
        }
        return interceptor;
    }
}
