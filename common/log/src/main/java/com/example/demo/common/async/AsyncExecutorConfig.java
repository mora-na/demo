package com.example.demo.common.async;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;

/**
 * 异步执行器配置，启用 MDC 透传。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = "common.async", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AsyncExecutorConfig {

    @Bean
    public TaskDecorator mdcTaskDecorator() {
        return new MdcTaskDecorator();
    }

    @Bean(name = {AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME, "applicationTaskExecutor"})
    @Primary
    @ConditionalOnMissingBean(name = {AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME,
            "applicationTaskExecutor"})
    public ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutionProperties properties,
                                                          ObjectProvider<TaskDecorator> decoratorProvider) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        TaskExecutionProperties.Pool pool = properties.getPool();
        executor.setCorePoolSize(pool.getCoreSize());
        executor.setMaxPoolSize(pool.getMaxSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setKeepAliveSeconds((int) pool.getKeepAlive().getSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(properties.getShutdown().isAwaitTermination());
        Duration await = properties.getShutdown().getAwaitTerminationPeriod();
        if (await != null) {
            executor.setAwaitTerminationSeconds((int) await.getSeconds());
        }
        TaskDecorator decorator = decoratorProvider.getIfAvailable();
        if (decorator != null) {
            executor.setTaskDecorator(decorator);
        }
        executor.initialize();
        return executor;
    }

}
