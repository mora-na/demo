package com.example.demo.extension.config;

import com.example.demo.extension.support.DynamicApiTaskDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 动态接口执行线程池配置。
 */
@Configuration
public class DynamicApiExecutorConfig {

    @Bean(name = "dynamicApiTaskExecutor")
    public ThreadPoolTaskExecutor dynamicApiTaskExecutor(DynamicApiProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        DynamicApiProperties.Executor pool = properties.getExecutor();
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setKeepAliveSeconds(pool.getKeepAliveSeconds());
        executor.setThreadNamePrefix(pool.getThreadNamePrefix());
        executor.setTaskDecorator(new DynamicApiTaskDecorator());
        executor.initialize();
        return executor;
    }
}
