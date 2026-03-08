package com.example.demo.job.handler.support;

import com.logcollect.core.context.LogCollectContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Spring @Async demo support for job logging.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Slf4j
@Component
public class AsyncLogTestSupport {

    @Async("jobAsyncExecutor")
    public CompletableFuture<Void> asyncLog(String runId, AsyncLogScenario scenario, String implementation) {
        return logAsync(runId, scenario, implementation);
    }

    @Async
    public CompletableFuture<Void> asyncLogWithDefaultConfigurer(String runId,
                                                                 AsyncLogScenario scenario,
                                                                 String implementation) {
        return logAsync(runId, scenario, implementation);
    }

    public ExecutorService wrapExecutorService(ExecutorService executorService) {
        // 手动创建的线程池不受 Spring 自动传播保护，必须显式包装。
        return LogCollectContextUtils.wrapExecutorService(executorService);
    }

    public Runnable wrapRunnable(Runnable runnable) {
        // 用于 new Thread / 回调 / ForkJoin 等“非 Spring 托管”线程入口。
        return LogCollectContextUtils.wrapRunnable(runnable);
    }

    public <V> Callable<V> wrapCallable(Callable<V> callable) {
        // 与 wrapRunnable 同理，补齐 submit(Callable) 等场景。
        return LogCollectContextUtils.wrapCallable(callable);
    }

    public <T> Consumer<T> wrapConsumer(Consumer<T> consumer) {
        // 用于 parallelStream / 各类回调型 API 的 Consumer 入口。
        return LogCollectContextUtils.wrapConsumer(consumer);
    }

    public <T, U> BiConsumer<T, U> wrapBiConsumer(BiConsumer<T, U> consumer) {
        // 用于 CompletableFuture.whenComplete 等双参回调入口。
        return LogCollectContextUtils.wrapBiConsumer(consumer);
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        // CompletableFuture 默认 commonPool 时，使用工具方法兜底上下文透传。
        return LogCollectContextUtils.runAsync(runnable);
    }

    public Thread newDaemonThread(Runnable runnable, String threadName) {
        // 直接创建线程时使用工具方法，确保子线程可继承采集上下文。
        return LogCollectContextUtils.newDaemonThread(runnable, threadName);
    }

    private CompletableFuture<Void> logAsync(String runId, AsyncLogScenario scenario, String implementation) {
        log.info("[AsyncJobTest][{}][{}] {} | thread={} | inCollectContext={}",
                runId,
                scenario.stage(),
                scenario.message(implementation),
                Thread.currentThread().getName(),
                LogCollectContextUtils.isInLogCollectContext());
        return CompletableFuture.completedFuture(null);
    }
}
