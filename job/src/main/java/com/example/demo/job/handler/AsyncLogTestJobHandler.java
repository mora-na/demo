package com.example.demo.job.handler;

import com.example.demo.job.api.JobContext;
import com.example.demo.job.api.JobHandler;
import com.example.demo.job.support.QuartzLogCollectHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.logcollect.api.annotation.LogCollect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Async logging demo job handler.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Slf4j
@Component("asyncLogTestJobHandler")
public class AsyncLogTestJobHandler implements JobHandler {

    private static final String LOG_PREFIX = "[AsyncJobTest]";
    private static final long ASYNC_WAIT_TIMEOUT_SECONDS = 10L;

    private final AsyncLogTestSupport asyncSupport;
    private final ThreadPoolTaskExecutor jobAsyncExecutor;
    private final ExecutorService manualExecutor;

    public AsyncLogTestJobHandler(AsyncLogTestSupport asyncSupport,
                                  @Qualifier("jobAsyncExecutor") ThreadPoolTaskExecutor jobAsyncExecutor) {
        this.asyncSupport = asyncSupport;
        this.jobAsyncExecutor = jobAsyncExecutor;
        // 手动线程池（非 Spring 管理）需使用工具类包装，避免线程切换后丢失 LogCollect 上下文。
        this.manualExecutor = asyncSupport.wrapExecutorService(
                Executors.newFixedThreadPool(2, newNamedThreadFactory("job-manual-"))
        );
    }

    private static ThreadFactory newNamedThreadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(0);
        return runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(prefix + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };
    }

    @Override
    @LogCollect(handler = QuartzLogCollectHandler.class, minLevel = "DEBUG")
    public void execute(JobContext context) {
        String runId = buildRunId(context);
        String contextSummary = buildContextSummary(context);
        List<CompletableFuture<?>> asyncStages = new ArrayList<>();

        logInfo(runId, "SYNC", "start " + contextSummary);
        logInfo(runId, "SYNC", "direct log in execute()");

        asyncStages.add(asyncSupport.asyncLog(runId, "SPRING_ASYNC", "@Async invoked"));

        // CompletableFuture 回调线程不固定，提前创建 wrapped 回调，确保任意线程执行时都能恢复采集上下文。
        Runnable springPoolSuccessCallback = asyncSupport.wrapRunnable(
                () -> logInfo(runId, "CF_SPRING_POOL", "CompletableFuture callback success")
        );
        AtomicReference<Throwable> springPoolCallbackError = new AtomicReference<>();
        Runnable springPoolErrorCallback = asyncSupport.wrapRunnable(
                () -> logWarn(runId, "CF_SPRING_POOL",
                        "CompletableFuture callback error: "
                                + resolveThrowableMessage(springPoolCallbackError.get()))
        );
        CompletableFuture<Void> springPoolFuture = CompletableFuture.runAsync(
                        () -> logInfo(runId, "CF_SPRING_POOL", "CompletableFuture.runAsync"),
                        jobAsyncExecutor
                )
                .whenComplete((ignored, ex) -> {
                    if (ex == null) {
                        springPoolSuccessCallback.run();
                    } else {
                        springPoolCallbackError.set(unwrapCompletionThrowable(ex));
                        springPoolErrorCallback.run();
                    }
                });
        asyncStages.add(springPoolFuture);

        // ListenableFuture 的 success/failure 回调同样可能在非业务线程触发，需提前包装回调逻辑。
        CompletableFuture<Void> listenableStage = new CompletableFuture<>();
        Runnable listenableSuccessCallback = asyncSupport.wrapRunnable(() -> {
            logInfo(runId, "SPRING_LISTENABLE", "callback success");
            listenableStage.complete(null);
        });
        AtomicReference<Throwable> listenableCallbackError = new AtomicReference<>();
        Runnable listenableErrorCallback = asyncSupport.wrapRunnable(() -> {
            Throwable callbackError = listenableCallbackError.get();
            logWarn(runId, "SPRING_LISTENABLE", "callback error: " + resolveThrowableMessage(callbackError));
            listenableStage.completeExceptionally(callbackError == null
                    ? new IllegalStateException("ListenableFuture callback error")
                    : callbackError);
        });
        ListenableFuture<?> listenableFuture = jobAsyncExecutor.submitListenable(
                () -> logInfo(runId, "SPRING_LISTENABLE", "ListenableFuture task")
        );
        listenableFuture.addCallback(
                result -> listenableSuccessCallback.run(),
                ex -> {
                    listenableCallbackError.set(unwrapCompletionThrowable(ex));
                    listenableErrorCallback.run();
                }
        );
        asyncStages.add(listenableStage);

        // 手动 ExecutorService 已在构造阶段 wrapExecutorService，一行兜底 submit/execute 全路径透传。
        asyncStages.add(CompletableFuture.runAsync(
                () -> logInfo(runId, "MANUAL_EXECUTOR", "Executors.newFixedThreadPool"),
                manualExecutor
        ));

        CompletableFuture<Void> rawThreadStage = new CompletableFuture<>();
        // 直接创建线程使用 newDaemonThread 包装，确保新线程恢复 LogCollect 上下文并在 finally 清理。
        Thread rawThread = asyncSupport.newDaemonThread(() -> {
            try {
                logInfo(runId, "RAW_THREAD", "new Thread()");
                rawThreadStage.complete(null);
            } catch (Throwable ex) {
                rawThreadStage.completeExceptionally(ex);
            }
        }, "job-raw-thread-" + runId);
        rawThread.start();
        asyncStages.add(rawThreadStage);

        for (int i = 0; i < 3; i++) {
            final int item = i;
            CompletableFuture<Void> forkJoinStage = new CompletableFuture<>();
            // ForkJoin/commonPool 非 Spring 托管入口，提交任务前用 wrapRunnable 包装。
            ForkJoinPool.commonPool().execute(asyncSupport.wrapRunnable(() -> {
                try {
                    logInfo(runId, "FORK_JOIN", "commonPool task item=" + item);
                    forkJoinStage.complete(null);
                } catch (Throwable ex) {
                    forkJoinStage.completeExceptionally(ex);
                }
            }));
            asyncStages.add(forkJoinStage);
        }

        CompletableFuture<Void> callbackStage = new CompletableFuture<>();
        AtomicReference<String> callbackDetailRef = new AtomicReference<>("Caffeine removal callback");
        // 第三方回调（Caffeine removalListener）提前创建 wrapped 回调，避免在未知线程触发时丢上下文。
        Runnable wrappedRemovalCallback = asyncSupport.wrapRunnable(() -> {
            try {
                logInfo(runId, "THIRD_PARTY_CALLBACK", callbackDetailRef.get());
                callbackStage.complete(null);
            } catch (Throwable ex) {
                callbackStage.completeExceptionally(ex);
            }
        });
        Cache<String, String> cache = Caffeine.newBuilder()
                .removalListener((key, value, cause) -> {
                    callbackDetailRef.set("Caffeine removal key=" + key + ", cause=" + cause);
                    jobAsyncExecutor.execute(wrappedRemovalCallback);
                })
                .build();
        cache.put("async-log", "value");
        cache.invalidate("async-log");
        asyncStages.add(callbackStage);

        waitForAsyncStages(runId, asyncStages);

        logInfo(runId, "SYNC", "dispatch finished");
    }

    @PreDestroy
    public void shutdown() {
        manualExecutor.shutdown();
        try {
            if (!manualExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                manualExecutor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            manualExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private String buildRunId(JobContext context) {
        String jobId = context == null ? null : String.valueOf(context.getJobId());
        if (jobId == null || "null".equalsIgnoreCase(jobId)) {
            jobId = "job";
        }
        return jobId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String buildContextSummary(JobContext context) {
        if (context == null) {
            return "jobContext=null";
        }
        return "jobId=" + safe(context.getJobId())
                + ", jobName=" + safe(context.getJobName())
                + ", handler=" + safe(context.getHandlerName())
                + ", cron=" + safe(context.getCronExpression())
                + ", now=" + LocalDateTime.now();
    }

    private String safe(Object value) {
        return Objects.toString(value, "-");
    }

    private void logInfo(String runId, String stage, String detail) {
        log.info("{}[{}][{}] {} | thread={}",
                LOG_PREFIX, runId, stage, detail, Thread.currentThread().getName());
    }

    private void logWarn(String runId, String stage, String detail) {
        log.warn("{}[{}][{}] {} | thread={}",
                LOG_PREFIX, runId, stage, detail, Thread.currentThread().getName());
    }

    private void waitForAsyncStages(String runId, List<CompletableFuture<?>> asyncStages) {
        if (asyncStages == null || asyncStages.isEmpty()) {
            return;
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(asyncStages.toArray(new CompletableFuture[0]));
        try {
            all.get(ASYNC_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logInfo(runId, "SYNC", "all async stages completed");
        } catch (TimeoutException ex) {
            logWarn(runId, "SYNC", "wait async stages timeout after " + ASYNC_WAIT_TIMEOUT_SECONDS + "s");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logWarn(runId, "SYNC", "wait async stages interrupted");
        } catch (Exception ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            logWarn(runId, "SYNC", "wait async stages error: " + resolveThrowableMessage(cause));
        }
    }

    private Throwable unwrapCompletionThrowable(Throwable throwable) {
        Throwable current = throwable;
        while ((current instanceof CompletionException || current instanceof ExecutionException)
                && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private String resolveThrowableMessage(Throwable throwable) {
        if (throwable == null) {
            return "unknown";
        }
        String message = throwable.getMessage();
        return message == null || message.trim().isEmpty()
                ? throwable.getClass().getSimpleName()
                : message;
    }
}
