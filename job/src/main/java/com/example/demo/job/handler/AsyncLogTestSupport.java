package com.example.demo.job.handler;

import com.logcollect.core.context.LogCollectContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
    public CompletableFuture<Void> asyncLog(String runId, String stage, String detail) {
        log.info("[AsyncJobTest][{}][{}] {} | thread={} | inCollectContext={}",
                runId,
                stage,
                detail,
                Thread.currentThread().getName(),
                LogCollectContextUtils.isInLogCollectContext());
        return CompletableFuture.completedFuture(null);
    }

    public ExecutorService wrapExecutorService(ExecutorService executorService) {
        return LogCollectContextUtils.wrapExecutorService(executorService);
    }

    public Runnable wrapRunnable(Runnable runnable) {
        return LogCollectContextUtils.wrapRunnable(runnable);
    }

    public Thread newDaemonThread(Runnable runnable, String threadName) {
        return LogCollectContextUtils.newDaemonThread(runnable, threadName);
    }
}
