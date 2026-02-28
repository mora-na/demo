package com.example.demo.job.handler;

import com.example.demo.job.api.JobContext;
import com.example.demo.job.api.JobHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
    public void execute(JobContext context) {
        String runId = buildRunId(context);
        String contextSummary = buildContextSummary(context);
        List<CompletableFuture<?>> asyncStages = new ArrayList<>();

        logInfo(runId, "SYNC", "start " + contextSummary);
        logInfo(runId, "SYNC", "direct log in execute()");

        asyncStages.add(asyncSupport.asyncLog(runId, "SPRING_ASYNC", "@Async invoked"));

        CompletableFuture<Void> springPoolFuture = CompletableFuture.runAsync(
                        () -> logInfo(runId, "CF_SPRING_POOL", "CompletableFuture.runAsync"),
                        jobAsyncExecutor
                )
                .whenComplete((ignored, ex) -> {
                    if (ex == null) {
                        logInfo(runId, "CF_SPRING_POOL", "CompletableFuture callback success");
                    } else {
                        logWarn(runId, "CF_SPRING_POOL", "CompletableFuture callback error: " + ex.getMessage());
                    }
                });
        asyncStages.add(springPoolFuture);

        CompletableFuture<Void> listenableStage = new CompletableFuture<>();
        ListenableFuture<?> listenableFuture = jobAsyncExecutor.submitListenable(
                () -> logInfo(runId, "SPRING_LISTENABLE", "ListenableFuture task")
        );
        listenableFuture.addCallback(
                result -> {
                    logInfo(runId, "SPRING_LISTENABLE", "callback success");
                    listenableStage.complete(null);
                },
                ex -> {
                    logWarn(runId, "SPRING_LISTENABLE", "callback error: " + ex.getMessage());
                    listenableStage.completeExceptionally(ex);
                }
        );
        asyncStages.add(listenableStage);

        asyncStages.add(CompletableFuture.runAsync(
                () -> logInfo(runId, "MANUAL_EXECUTOR", "Executors.newFixedThreadPool"),
                manualExecutor
        ));

        CompletableFuture<Void> rawThreadStage = new CompletableFuture<>();
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
        Cache<String, String> cache = Caffeine.newBuilder()
                .removalListener((key, value, cause) -> jobAsyncExecutor.execute(
                        asyncSupport.wrapRunnable(() -> {
                            try {
                                logInfo(runId,
                                        "THIRD_PARTY_CALLBACK",
                                        "Caffeine removal key=" + key + ", cause=" + cause);
                                callbackStage.complete(null);
                            } catch (Throwable ex) {
                                callbackStage.completeExceptionally(ex);
                            }
                        })
                ))
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
            String message = cause.getMessage();
            logWarn(runId, "SYNC", "wait async stages error: "
                    + (message == null ? cause.getClass().getSimpleName() : message));
        }
    }
}
