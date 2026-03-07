package com.example.demo.job.config;

import com.logcollect.core.context.LogCollectContextUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Optional custom AsyncConfigurer profile for validating @Async default-executor routing.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/7
 */
@Configuration
@Profile("job-async-custom")
public class JobCustomAsyncConfigurer implements AsyncConfigurer {

    @Bean(name = "jobCustomAsyncExecutor")
    public ThreadPoolTaskExecutor jobCustomAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("job-custom-async-");
        executor.setTaskDecorator(LogCollectContextUtils::wrapRunnable);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return jobCustomAsyncExecutor();
    }
}
