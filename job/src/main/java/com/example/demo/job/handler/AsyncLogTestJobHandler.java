package com.example.demo.job.handler;

import com.example.demo.job.api.JobContext;
import com.example.demo.job.api.JobHandler;
import com.example.demo.job.config.AsyncLogCoverageMode;
import com.example.demo.job.config.AsyncLogCoverageProperties;
import com.example.demo.job.handler.support.AsyncLogCustomConfigurerProbe;
import com.example.demo.job.handler.support.AsyncLogScenario;
import com.example.demo.job.handler.support.AsyncLogTestSupport;
import com.example.demo.job.handler.support.AsyncNestedLogCollectProbe;
import com.example.demo.job.support.QuartzLogCollectHandler;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.logcollect.api.annotation.LogCollect;
import com.logcollect.core.context.LogCollectContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootVersion;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

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
    private static final long BRANCH_STAGE_AWAIT_TIMEOUT_SECONDS = 5L;
    private static final String SERVLET_ASYNC_SOURCE = "/jobs/async-log/servlet-async";
    private static final String DIRECT_SOURCE = "/jobs/async-log/direct";

    private final AsyncLogTestSupport asyncSupport;
    private final AsyncNestedLogCollectProbe nestedLogCollectProbe;
    private final ObjectProvider<AsyncLogCustomConfigurerProbe> customConfigurerProbeProvider;
    private final AsyncLogCoverageProperties coverageProperties;
    private final ThreadPoolTaskExecutor jobAsyncExecutor;
    private final ExecutorService springBeanExecutorService;
    private final ExecutorService manualExecutor;

    public AsyncLogTestJobHandler(AsyncLogTestSupport asyncSupport,
                                  AsyncNestedLogCollectProbe nestedLogCollectProbe,
                                  ObjectProvider<AsyncLogCustomConfigurerProbe> customConfigurerProbeProvider,
                                  AsyncLogCoverageProperties coverageProperties,
                                  @Qualifier("jobAsyncExecutor") ThreadPoolTaskExecutor jobAsyncExecutor,
                                  @Qualifier("jobBeanExecutorService") ExecutorService springBeanExecutorService) {
        this.asyncSupport = asyncSupport;
        this.nestedLogCollectProbe = nestedLogCollectProbe;
        this.customConfigurerProbeProvider = customConfigurerProbeProvider;
        this.coverageProperties = coverageProperties;
        this.jobAsyncExecutor = jobAsyncExecutor;
        this.springBeanExecutorService = springBeanExecutorService;
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
    public void execute(JobContext context) throws Exception {
        String runId = buildRunId(context);
        logInfo(runId, AsyncLogScenario.RUN_START,
                "AsyncLogTestJobHandler.execute(JobContext)",
                buildScheduledContextSummary(context));
        logInfo(runId, AsyncLogScenario.SYNC_DIRECT,
                "execute() 主线程直接输出",
                null);
        executeScheduledCoverage(runId, context);
        logInfo(runId, AsyncLogScenario.RUN_FINISHED,
                "AsyncLogTestJobHandler.execute(JobContext)",
                null);
    }

    @LogCollect(handler = QuartzLogCollectHandler.class, minLevel = "DEBUG")
    public void logTest(JobContext context) {
        String runId = buildRunId(context);
        logInfo(runId, AsyncLogScenario.RUN_START,
                "AsyncLogTestJobHandler.execute(JobContext)",
                buildContextSummary(context));
        logInfo(runId, AsyncLogScenario.SYNC_DIRECT,
                "execute() 主线程直接输出",
                null);

        AsyncLogCoverageMode mode = coverageProperties.getModeEnum();
        if (mode == AsyncLogCoverageMode.BRANCHES) {
            executeBranchCoverage(runId, context);
        } else {
            executeBaselineCoverage(runId, context);
        }

        logInfo(runId, AsyncLogScenario.RUN_FINISHED,
                "AsyncLogTestJobHandler.execute(JobContext)",
                null);
    }

    private void executeScheduledCoverage(String runId, JobContext context) {
        List<CompletableFuture<?>> asyncStages = new ArrayList<>();
        probeNestedLogCollect(runId, context);
        executeScheduledAsyncConfigurerProbe(runId, asyncStages);

        asyncStages.add(asyncSupport.asyncLog(
                runId,
                AsyncLogScenario.SPRING_THREAD_POOL_ASYNC,
                "@Async(\"jobAsyncExecutor\")"
        ));
        asyncStages.add(runSpringBeanExecutorServiceProbe(runId));
        asyncStages.add(runCompletableFutureSuccessProbe(runId));
        asyncStages.add(runListenableFutureSuccessProbe(runId));
        asyncStages.add(runManualExecutorProbe(runId));
        asyncStages.add(runRawThreadProbe(runId));
        asyncStages.addAll(runForkJoinCommonPoolProbes(runId));
        asyncStages.add(runParallelStreamProbe(runId));
        asyncStages.add(runThirdPartyCallbackProbe(runId));
        asyncStages.add(runReactorMonoFluxProbe(runId));

        logInfo(runId, AsyncLogScenario.WEBFLUX_NOTE,
                "Reactor publishOn + Spring Scheduler",
                "currentBoot=" + currentBootVersion());
        waitForAsyncStages(runId,
                "scheduled.asyncStages",
                asyncStages,
                ASYNC_WAIT_TIMEOUT_SECONDS);
    }

    private void executeBaselineCoverage(String runId, JobContext context) {
        ensureBaselineRuntime(context);

        List<CompletableFuture<?>> asyncStages = new ArrayList<>();
        probeNestedLogCollect(runId, context);

        asyncStages.add(asyncSupport.asyncLogWithDefaultConfigurer(
                runId,
                AsyncLogScenario.SPRING_ASYNC_DEFAULT,
                "@Async 默认执行器"
        ));
        logInfo(runId, AsyncLogScenario.SPRING_ASYNC_CUSTOM_SKIP,
                "profile job-async-custom 未启用",
                null);

        asyncStages.add(asyncSupport.asyncLog(
                runId,
                AsyncLogScenario.SPRING_THREAD_POOL_ASYNC,
                "@Async(\"jobAsyncExecutor\")"
        ));
        asyncStages.add(runSpringBeanExecutorServiceProbe(runId));
        asyncStages.add(runCompletableFutureSuccessProbe(runId));
        asyncStages.add(runListenableFutureSuccessProbe(runId));
        asyncStages.add(runManualExecutorProbe(runId));
        asyncStages.add(runRawThreadProbe(runId));
        asyncStages.addAll(runForkJoinCommonPoolProbes(runId));
        asyncStages.add(runParallelStreamProbe(runId));
        asyncStages.add(runThirdPartyCallbackProbe(runId));
        asyncStages.add(runReactorMonoFluxProbe(runId));

        logInfo(runId, AsyncLogScenario.WEBFLUX_NOTE,
                "Reactor publishOn + Spring Scheduler",
                "currentBoot=" + currentBootVersion());
        logServletAsyncScenario(runId, context);
        waitForAsyncStages(runId,
                "baseline.asyncStages",
                asyncStages,
                ASYNC_WAIT_TIMEOUT_SECONDS);
    }

    private void executeScheduledAsyncConfigurerProbe(String runId, List<CompletableFuture<?>> asyncStages) {
        AsyncLogCustomConfigurerProbe customConfigurerProbe = customConfigurerProbeProvider.getIfAvailable();
        if (customConfigurerProbe == null) {
            asyncStages.add(asyncSupport.asyncLogWithDefaultConfigurer(
                    runId,
                    AsyncLogScenario.SPRING_ASYNC_DEFAULT,
                    "@Async 默认执行器"
            ));
            logInfo(runId, AsyncLogScenario.SPRING_ASYNC_CUSTOM_SKIP,
                    "profile job-async-custom 未启用",
                    null);
            return;
        }
        logInfo(runId, AsyncLogScenario.SPRING_ASYNC_DEFAULT_SKIP,
                "AsyncConfigurer#getAsyncExecutor 已覆盖默认路径",
                null);
        asyncStages.add(customConfigurerProbe.asyncLog(
                runId,
                AsyncLogScenario.SPRING_ASYNC_CUSTOM,
                "@Async + 自定义 AsyncConfigurer"
        ));
    }

    private void executeBranchCoverage(String runId, JobContext context) {
        AsyncLogCustomConfigurerProbe customConfigurerProbe = requireBranchRuntime(context);
        probeNestedLogCollect(runId, null);

        logInfo(runId, AsyncLogScenario.SPRING_ASYNC_DEFAULT_SKIP,
                "AsyncConfigurer#getAsyncExecutor 已覆盖默认路径",
                null);
        logServletAsyncScenario(runId, context);

        List<CompletableFuture<?>> branchStages = new ArrayList<>();
        branchStages.add(customConfigurerProbe.asyncLog(
                runId,
                AsyncLogScenario.SPRING_ASYNC_CUSTOM,
                "@Async + 自定义 AsyncConfigurer"
        ));
        branchStages.add(runCompletableFutureErrorProbe(runId));
        branchStages.add(runListenableFutureErrorProbe(runId));
        branchStages.add(runReactorSetupErrorProbe(runId));
        awaitBranchStages(runId, branchStages);

        probeWaitError(runId);
        probeWaitTimeout(runId);
        probeWaitInterrupted(runId);
    }

    private void ensureBaselineRuntime(JobContext context) {
        if (customConfigurerProbeProvider.getIfAvailable() != null) {
            throw new IllegalStateException("Coverage mode 'baseline' requires profile job-async-custom to be disabled");
        }
        if (!isServletAsyncProbeContext(context)) {
            throw new IllegalStateException("Coverage mode 'baseline' requires /jobs/async-log/servlet-async entry");
        }
    }

    private AsyncLogCustomConfigurerProbe requireBranchRuntime(JobContext context) {
        AsyncLogCustomConfigurerProbe customConfigurerProbe = customConfigurerProbeProvider.getIfAvailable();
        if (customConfigurerProbe == null) {
            throw new IllegalStateException("Coverage mode 'branches' requires profile job-async-custom to be enabled");
        }
        if (isServletAsyncProbeContext(context)) {
            throw new IllegalStateException("Coverage mode 'branches' requires /jobs/async-log/direct entry");
        }
        return customConfigurerProbe;
    }

    private void probeNestedLogCollect(String runId, JobContext context) {
        if (context != null) {
            nestedLogCollectProbe.nestedLog(context, runId);
            return;
        }
        logInfo(runId, AsyncLogScenario.NESTED_LOGCOLLECT_SKIP,
                "nestedLogCollectProbe.nestedLog(...)",
                "jobContext=null");
    }

    private CompletableFuture<Void> runSpringBeanExecutorServiceProbe(String runId) {
        return CompletableFuture.runAsync(
                () -> logInfo(runId, AsyncLogScenario.SPRING_BEAN_EXECUTOR_SERVICE,
                        "@Qualifier(\"jobBeanExecutorService\") ExecutorService",
                        null),
                springBeanExecutorService
        );
    }

    private CompletableFuture<Void> runCompletableFutureSuccessProbe(String runId) {
        BiConsumer<Void, Throwable> completionCallback = asyncSupport.wrapBiConsumer((ignored, ex) -> {
            if (ex == null) {
                logInfo(runId, AsyncLogScenario.CF_SPRING_POOL_CALLBACK_SUCCESS,
                        "CompletableFuture.whenComplete(success)",
                        null);
                return;
            }
            logWarn(runId, AsyncLogScenario.CF_SPRING_POOL_CALLBACK_ERROR,
                    "CompletableFuture.whenComplete(error)",
                    "error=" + resolveThrowableMessage(unwrapCompletionThrowable(ex)));
        });
        return CompletableFuture.runAsync(
                        () -> logInfo(runId, AsyncLogScenario.CF_SPRING_POOL_TASK,
                                "CompletableFuture.runAsync(..., jobAsyncExecutor)",
                                null),
                        jobAsyncExecutor
                )
                .whenComplete(completionCallback);
    }

    private CompletableFuture<Void> runCompletableFutureErrorProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        BiConsumer<Void, Throwable> completionCallback = asyncSupport.wrapBiConsumer((ignored, ex) -> {
            if (ex == null) {
                stage.completeExceptionally(new IllegalStateException("CompletableFuture error probe finished without exception"));
                return;
            }
            logWarn(runId, AsyncLogScenario.CF_SPRING_POOL_CALLBACK_ERROR,
                    "CompletableFuture.whenComplete(error)",
                    "error=" + resolveThrowableMessage(unwrapCompletionThrowable(ex)));
            stage.complete(null);
        });
        CompletableFuture.runAsync(
                        () -> {
                            throw new IllegalStateException("coverage-branches-completableFuture-error");
                        },
                        jobAsyncExecutor
                )
                .whenComplete(completionCallback);
        return stage;
    }

    private CompletableFuture<Void> runListenableFutureSuccessProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        Runnable successCallback = asyncSupport.wrapRunnable(() -> {
            logInfo(runId, AsyncLogScenario.SPRING_THREAD_POOL_LISTENABLE_SUCCESS,
                    "ListenableFuture.addCallback(success)",
                    null);
            stage.complete(null);
        });
        AtomicReference<Throwable> callbackError = new AtomicReference<>();
        Runnable errorCallback = asyncSupport.wrapRunnable(() -> {
            Throwable callbackThrowable = callbackError.get();
            logWarn(runId, AsyncLogScenario.SPRING_THREAD_POOL_LISTENABLE_ERROR,
                    "ListenableFuture.addCallback(failure)",
                    "error=" + resolveThrowableMessage(callbackThrowable));
            stage.completeExceptionally(callbackThrowable == null
                    ? new IllegalStateException("ListenableFuture callback error")
                    : callbackThrowable);
        });
        ListenableFuture<?> listenableFuture = jobAsyncExecutor.submitListenable(
                () -> logInfo(runId, AsyncLogScenario.SPRING_THREAD_POOL_LISTENABLE_TASK,
                        "ThreadPoolTaskExecutor.submitListenable(...)",
                        null)
        );
        listenableFuture.addCallback(
                result -> successCallback.run(),
                ex -> {
                    callbackError.set(unwrapCompletionThrowable(ex));
                    errorCallback.run();
                }
        );
        return stage;
    }

    private CompletableFuture<Void> runListenableFutureErrorProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        AtomicReference<Throwable> callbackError = new AtomicReference<>();
        Runnable errorCallback = asyncSupport.wrapRunnable(() -> {
            logWarn(runId, AsyncLogScenario.SPRING_THREAD_POOL_LISTENABLE_ERROR,
                    "ListenableFuture.addCallback(failure)",
                    "error=" + resolveThrowableMessage(callbackError.get()));
            stage.complete(null);
        });
        ListenableFuture<?> listenableFuture = jobAsyncExecutor.submitListenable(
                () -> {
                    throw new IllegalStateException("coverage-branches-listenableFuture-error");
                }
        );
        listenableFuture.addCallback(
                result -> stage.completeExceptionally(new IllegalStateException("ListenableFuture error probe finished without exception")),
                ex -> {
                    callbackError.set(unwrapCompletionThrowable(ex));
                    errorCallback.run();
                }
        );
        return stage;
    }

    private CompletableFuture<Void> runManualExecutorProbe(String runId) {
        return CompletableFuture.runAsync(
                () -> logInfo(runId, AsyncLogScenario.MANUAL_EXECUTOR,
                        "LogCollectContextUtils.wrapExecutorService(Executors.newFixedThreadPool)",
                        null),
                manualExecutor
        );
    }

    private CompletableFuture<Void> runRawThreadProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        Thread rawThread = asyncSupport.newDaemonThread(() -> {
            try {
                logInfo(runId, AsyncLogScenario.RAW_THREAD,
                        "LogCollectContextUtils.newDaemonThread(...)",
                        null);
                stage.complete(null);
            } catch (Throwable ex) {
                stage.completeExceptionally(ex);
            }
        }, "job-raw-thread-" + runId);
        rawThread.start();
        return stage;
    }

    private List<CompletableFuture<?>> runForkJoinCommonPoolProbes(String runId) {
        List<CompletableFuture<?>> stages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            final int item = i;
            CompletableFuture<Void> stage = new CompletableFuture<>();
            ForkJoinPool.commonPool().execute(asyncSupport.wrapRunnable(() -> {
                try {
                    logInfo(runId, AsyncLogScenario.FORK_JOIN_COMMON_POOL,
                            "ForkJoinPool.commonPool().execute(...)",
                            "item=" + item);
                    stage.complete(null);
                } catch (Throwable ex) {
                    stage.completeExceptionally(ex);
                }
            }));
            stages.add(stage);
        }
        return stages;
    }

    private CompletableFuture<Void> runParallelStreamProbe(String runId) {
        return asyncSupport.runAsync(
                () -> IntStream.range(0, 3)
                        .boxed()
                        .parallel()
                        .forEach(asyncSupport.wrapConsumer(item -> logInfo(
                                runId,
                                AsyncLogScenario.FORK_JOIN_PARALLEL_STREAM,
                                "IntStream.range(...).parallel().forEach(...)",
                                "item=" + item
                        )))
        );
    }

    private CompletableFuture<Void> runThirdPartyCallbackProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        AtomicReference<String> callbackDetailRef = new AtomicReference<>("Caffeine removal callback");
        Runnable wrappedRemovalCallback = asyncSupport.wrapRunnable(() -> {
            try {
                logInfo(runId, AsyncLogScenario.THIRD_PARTY_CALLBACK,
                        "Caffeine.removalListener -> wrapped callback",
                        callbackDetailRef.get());
                stage.complete(null);
            } catch (Throwable ex) {
                stage.completeExceptionally(ex);
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
        return stage;
    }

    private CompletableFuture<Void> runReactorMonoFluxProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        try {
            Scheduler scheduler = Schedulers.fromExecutor(jobAsyncExecutor);
            Mono<Void> monoStage = Mono.just("mono")
                    .publishOn(scheduler)
                    .doOnNext(item -> logInfo(runId, AsyncLogScenario.WEBFLUX_MONO,
                            "Mono.publishOn(Schedulers.fromExecutor(jobAsyncExecutor))",
                            "item=" + item))
                    .then();
            Mono<Void> fluxStage = Flux.range(0, 2)
                    .publishOn(scheduler)
                    .doOnNext(item -> logInfo(runId, AsyncLogScenario.WEBFLUX_FLUX,
                            "Flux.publishOn(Schedulers.fromExecutor(jobAsyncExecutor))",
                            "item=" + item))
                    .then();
            Mono.when(monoStage, fluxStage)
                    .doOnSuccess(ignore -> stage.complete(null))
                    .doOnError(stage::completeExceptionally)
                    .subscribe();
        } catch (Throwable ex) {
            stage.completeExceptionally(ex);
            logWarn(runId, AsyncLogScenario.WEBFLUX_SETUP_ERROR,
                    "Mono.when(monoStage, fluxStage).subscribe()",
                    "error=" + resolveThrowableMessage(ex));
        }
        return stage;
    }

    private CompletableFuture<Void> runReactorSetupErrorProbe(String runId) {
        CompletableFuture<Void> stage = new CompletableFuture<>();
        try {
            throw new IllegalStateException("coverage-branches-reactor-setup-error");
        } catch (Throwable ex) {
            logWarn(runId, AsyncLogScenario.WEBFLUX_SETUP_ERROR,
                    "Mono.when(monoStage, fluxStage).subscribe()",
                    "error=" + resolveThrowableMessage(ex));
            stage.complete(null);
        }
        return stage;
    }

    private void logServletAsyncScenario(String runId, JobContext context) {
        String source = resolveContextSource(context);
        if (isServletAsyncProbeContext(context)) {
            logInfo(runId, AsyncLogScenario.SERVLET_ASYNC_ACTIVE,
                    "POST " + SERVLET_ASYNC_SOURCE + " -> WebAsyncTask",
                    "source=" + source);
            return;
        }
        logInfo(runId, AsyncLogScenario.SERVLET_ASYNC_NOTE,
                "non-servlet-async entry",
                "source=" + source);
    }

    private void probeWaitError(String runId) {
        CompletableFuture<Void> failedStage = new CompletableFuture<>();
        failedStage.completeExceptionally(new IllegalStateException("coverage-branches-wait-error"));
        List<CompletableFuture<?>> stages = new ArrayList<>();
        stages.add(failedStage);
        waitForAsyncStages(runId,
                "branches.wait.error",
                stages,
                coverageProperties.getBranchWaitTimeoutSeconds());
    }

    private void probeWaitTimeout(String runId) {
        CompletableFuture<Void> neverEndingStage = new CompletableFuture<>();
        List<CompletableFuture<?>> stages = new ArrayList<>();
        stages.add(neverEndingStage);
        waitForAsyncStages(runId,
                "branches.wait.timeout",
                stages,
                coverageProperties.getBranchWaitTimeoutSeconds());
    }

    private void probeWaitInterrupted(String runId) {
        CompletableFuture<Void> neverEndingStage = new CompletableFuture<>();
        List<CompletableFuture<?>> stages = new ArrayList<>();
        stages.add(neverEndingStage);
        Thread waitingThread = Thread.currentThread();
        Thread interrupter = asyncSupport.newDaemonThread(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
                waitingThread.interrupt();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }, "job-branch-interrupt-" + runId);
        interrupter.start();
        waitForAsyncStages(runId,
                "branches.wait.interrupted",
                stages,
                Math.max(coverageProperties.getBranchWaitTimeoutSeconds(), 1L));
        Thread.interrupted();
    }

    private void awaitBranchStages(String runId, List<CompletableFuture<?>> stages) {
        if (stages == null || stages.isEmpty()) {
            return;
        }
        try {
            CompletableFuture.allOf(stages.toArray(new CompletableFuture[0]))
                    .get(BRANCH_STAGE_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Branch coverage stages did not complete cleanly for runId=" + runId, ex);
        }
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
            return "jobContext=null, coverageMode=" + coverageProperties.getModeEnum().getValue();
        }
        return "jobId=" + safe(context.getJobId())
                + ", jobName=" + safe(context.getJobName())
                + ", handler=" + safe(context.getHandlerName())
                + ", cron=" + safe(context.getCronExpression())
                + ", source=" + resolveContextSource(context)
                + ", coverageMode=" + coverageProperties.getModeEnum().getValue()
                + ", now=" + LocalDateTime.now();
    }

    private String buildScheduledContextSummary(JobContext context) {
        if (context == null) {
            return "jobContext=null, configuredLogTestMode=" + coverageProperties.getModeEnum().getValue()
                    + ", trigger=quartz-scheduled, scope=schedulable-only";
        }
        return "jobId=" + safe(context.getJobId())
                + ", jobName=" + safe(context.getJobName())
                + ", handler=" + safe(context.getHandlerName())
                + ", cron=" + safe(context.getCronExpression())
                + ", source=" + resolveContextSource(context)
                + ", configuredLogTestMode=" + coverageProperties.getModeEnum().getValue()
                + ", trigger=quartz-scheduled, scope=schedulable-only"
                + ", now=" + LocalDateTime.now();
    }

    private String safe(Object value) {
        return Objects.toString(value, "-");
    }

    private void logInfo(String runId, AsyncLogScenario scenario, String implementation, String extra) {
        log.info("{}[{}][{}] {} | thread={} | inCollectContext={}",
                LOG_PREFIX,
                runId,
                scenario.stage(),
                scenario.message(implementation, extra),
                Thread.currentThread().getName(),
                LogCollectContextUtils.isInLogCollectContext());
    }

    private void logWarn(String runId, AsyncLogScenario scenario, String implementation, String extra) {
        log.warn("{}[{}][{}] {} | thread={} | inCollectContext={}",
                LOG_PREFIX,
                runId,
                scenario.stage(),
                scenario.message(implementation, extra),
                Thread.currentThread().getName(),
                LogCollectContextUtils.isInLogCollectContext());
    }

    private void waitForAsyncStages(String runId,
                                    String probeName,
                                    List<CompletableFuture<?>> asyncStages,
                                    long timeoutSeconds) {
        if (asyncStages == null || asyncStages.isEmpty()) {
            return;
        }
        CompletableFuture<Void> all = CompletableFuture.allOf(asyncStages.toArray(new CompletableFuture[0]));
        try {
            all.get(timeoutSeconds, TimeUnit.SECONDS);
            logInfo(runId, AsyncLogScenario.RUN_ALL_COMPLETED,
                    probeName + " -> CompletableFuture.allOf(...).get(...)",
                    "stageCount=" + asyncStages.size());
        } catch (TimeoutException ex) {
            logWarn(runId, AsyncLogScenario.RUN_WAIT_TIMEOUT,
                    probeName + " -> CompletableFuture.allOf(...).get(timeout)",
                    "timeoutSeconds=" + timeoutSeconds);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logWarn(runId, AsyncLogScenario.RUN_WAIT_INTERRUPTED,
                    probeName + " -> CompletableFuture.allOf(...).get(...)",
                    null);
        } catch (Exception ex) {
            Throwable cause = ex.getCause() == null ? ex : ex.getCause();
            logWarn(runId, AsyncLogScenario.RUN_WAIT_ERROR,
                    probeName + " -> CompletableFuture.allOf(...).get(...)",
                    "error=" + resolveThrowableMessage(cause));
        }
    }

    private boolean isServletAsyncProbeContext(JobContext context) {
        return resolveContextSource(context).contains(SERVLET_ASYNC_SOURCE);
    }

    private String resolveContextSource(JobContext context) {
        if (context == null || context.getParams() == null) {
            return "unknown";
        }
        if (context.getParams().contains(SERVLET_ASYNC_SOURCE)) {
            return SERVLET_ASYNC_SOURCE;
        }
        if (context.getParams().contains(DIRECT_SOURCE)) {
            return DIRECT_SOURCE;
        }
        return context.getParams();
    }

    private String currentBootVersion() {
        String version = SpringBootVersion.getVersion();
        return version == null || version.trim().isEmpty() ? "unknown" : version;
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
