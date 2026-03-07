package com.example.demo.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Job module async executor configuration.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Configuration
@EnableAsync
public class JobAsyncConfig {

    @Bean(name = "jobAsyncExecutor")
    public ThreadPoolTaskExecutor jobAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("job-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        return executor;
    }

    @Bean(name = "jobBeanExecutorService", destroyMethod = "shutdown")
    public ExecutorService jobBeanExecutorService() {
        return Executors.newFixedThreadPool(2, newNamedThreadFactory("job-bean-executor-"));
    }

    private ThreadFactory newNamedThreadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(0);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }
}
