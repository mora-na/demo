package com.example.demo.extension.config;

import com.example.demo.extension.support.DynamicApiTaskDecorator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 动态接口执行线程池配置。
 */
@Configuration
public class DynamicApiExecutorConfig {

    @Bean(name = "dynamicApiTaskExecutor")
    public ThreadPoolTaskExecutor dynamicApiTaskExecutor(DynamicApiProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        DynamicApiProperties.Executor pool = properties.getExecutor();
        applyExecutorConfig(executor, pool);
        executor.setTaskDecorator(new DynamicApiTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean(name = "dynamicApiRouteExecutors")
    public Map<String, ThreadPoolTaskExecutor> dynamicApiRouteExecutors(DynamicApiProperties properties) {
        if (properties == null || properties.getExecutors() == null || properties.getExecutors().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, ThreadPoolTaskExecutor> executors = new LinkedHashMap<>();
        for (Map.Entry<String, DynamicApiProperties.Executor> entry : properties.getExecutors().entrySet()) {
            String name = entry.getKey();
            DynamicApiProperties.Executor config = entry.getValue();
            if (StringUtils.isBlank(name) || config == null) {
                continue;
            }
            ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
            applyExecutorConfig(executor, config);
            executor.setTaskDecorator(new DynamicApiTaskDecorator());
            executor.initialize();
            executors.put(name, executor);
        }
        return executors;
    }

    @Bean(name = "dynamicApiCleanupExecutor")
    public ThreadPoolTaskExecutor dynamicApiCleanupExecutor(DynamicApiConstants constants) {
        DynamicApiConstants.Execute execute = constants.getExecute();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(execute.getCleanupExecutorCorePoolSize());
        executor.setMaxPoolSize(execute.getCleanupExecutorMaxPoolSize());
        executor.setQueueCapacity(execute.getCleanupExecutorQueueCapacity());
        executor.setKeepAliveSeconds(execute.getCleanupExecutorKeepAliveSeconds());
        executor.setThreadNamePrefix(execute.getCleanupExecutorThreadNamePrefix());
        executor.setTaskDecorator(new DynamicApiTaskDecorator());
        executor.initialize();
        return executor;
    }

    @Bean(name = "dynamicApiCleanupScheduler")
    public ScheduledExecutorService dynamicApiCleanupScheduler(DynamicApiConstants constants) {
        DynamicApiConstants.Execute execute = constants.getExecute();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                Math.max(1, execute.getCleanupSchedulerPoolSize()),
                new NamedThreadFactory(execute.getCleanupSchedulerThreadNamePrefix())
        );
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    private void applyExecutorConfig(ThreadPoolTaskExecutor executor, DynamicApiProperties.Executor pool) {
        if (executor == null || pool == null) {
            return;
        }
        executor.setCorePoolSize(pool.getCorePoolSize());
        executor.setMaxPoolSize(pool.getMaxPoolSize());
        executor.setQueueCapacity(pool.getQueueCapacity());
        executor.setKeepAliveSeconds(pool.getKeepAliveSeconds());
        executor.setThreadNamePrefix(pool.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(resolveRejectedHandler(pool.getRejectedPolicy()));
    }

    private RejectedExecutionHandler resolveRejectedHandler(String policy) {
        if (policy == null) {
            return new ThreadPoolExecutor.AbortPolicy();
        }
        String normalized = policy.trim().toUpperCase();
        switch (normalized) {
            case "CALLER_RUNS":
                return new ThreadPoolExecutor.CallerRunsPolicy();
            case "DISCARD":
                return new ThreadPoolExecutor.DiscardPolicy();
            case "DISCARD_OLDEST":
                return new ThreadPoolExecutor.DiscardOldestPolicy();
            default:
                return new ThreadPoolExecutor.AbortPolicy();
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String prefix;
        private final AtomicInteger index = new AtomicInteger(1);

        private NamedThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.setName(prefix + index.getAndIncrement());
            return thread;
        }
    }
}
