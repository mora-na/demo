package com.example.demo.common.config;

import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public ISqlInjector mppSqlInjector() {
        return new MppSqlInjector();
    }
}
