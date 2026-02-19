package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.metrics.DynamicApiMetrics;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 动态接口执行器。
 */
@Slf4j
@Component
public class DynamicApiExecutor {

    private final ThreadPoolTaskExecutor executor;
    private final ThreadPoolTaskExecutor cleanupExecutor;
    private final ScheduledExecutorService cleanupScheduler;
    private final ExecuteStrategyFactory strategyFactory;
    private final DynamicApiConstants constants;
    private final DynamicApiExecutorRouter router;
    private final DynamicApiCircuitBreaker circuitBreaker;
    private final DynamicApiMetrics metrics;

    public DynamicApiExecutor(@Qualifier("dynamicApiTaskExecutor") ThreadPoolTaskExecutor executor,
                              @Qualifier("dynamicApiCleanupExecutor") ThreadPoolTaskExecutor cleanupExecutor,
                              @Qualifier("dynamicApiCleanupScheduler") ScheduledExecutorService cleanupScheduler,
                              ExecuteStrategyFactory strategyFactory,
                              DynamicApiConstants constants,
                              DynamicApiExecutorRouter router,
                              DynamicApiCircuitBreaker circuitBreaker,
                              DynamicApiMetrics metrics) {
        this.executor = executor;
        this.cleanupExecutor = cleanupExecutor;
        this.cleanupScheduler = cleanupScheduler;
        this.strategyFactory = strategyFactory;
        this.constants = constants;
        this.router = router;
        this.circuitBreaker = circuitBreaker;
        this.metrics = metrics;
    }

    public DynamicApiExecution submit(DynamicApiContext context) {
        if (context == null || context.getMeta() == null) {
            return new DynamicApiExecution(
                    CompletableFuture.completedFuture(
                            DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                                    constants.getMessage().getExecuteFailed())),
                    null);
        }
        String type = context.getMeta().getType();
        ExecuteStrategy strategy = strategyFactory.get(type);
        if (strategy == null) {
            return new DynamicApiExecution(
                    CompletableFuture.completedFuture(
                            DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                                    constants.getMessage().getTypeInvalid())),
                    null);
        }
        if (circuitBreaker != null && !circuitBreaker.allow(context.getMeta())) {
            if (metrics != null) {
                metrics.recordReject(context.getMeta(), DynamicApiTerminationReason.CIRCUIT_OPEN);
            }
            return new DynamicApiExecution(
                    CompletableFuture.completedFuture(
                            DynamicApiExecuteResult.error(constants.getController().getServiceUnavailableCode(),
                                    constants.getMessage().getCircuitOpen(),
                                    DynamicApiTerminationReason.CIRCUIT_OPEN)),
                    null);
        }
        CompletableFuture<DynamicApiExecuteResult> resultFuture = new CompletableFuture<>();
        AtomicBoolean cleanupInvoked = new AtomicBoolean(false);
        AtomicBoolean completionRecorded = new AtomicBoolean(false);
        AtomicLong startNanos = new AtomicLong(0L);
        long submitNanos = System.nanoTime();
        if (metrics != null) {
            metrics.recordSubmit(context.getMeta());
        }
        ThreadPoolTaskExecutor taskExecutor = router == null ? executor : router.select(context.getMeta());
        Future<?> taskFuture;
        try {
            taskFuture = taskExecutor.submit(() -> {
                long start = System.nanoTime();
                startNanos.set(start);
                if (metrics != null) {
                    metrics.recordStart(context.getMeta(), toMillis(start - submitNanos));
                }
                boolean success = false;
                DynamicApiTerminationReason reason = null;
                try {
                    DynamicApiExecuteResult result = strategy.execute(context);
                    success = result != null && result.isSuccess();
                    reason = resolveReason(result);
                    resultFuture.complete(result);
                } catch (Throwable ex) {
                    if (resultFuture.isDone()) {
                        return;
                    }
                    if (!resultFuture.isDone()) {
                        invokeCleanupOnce(strategy, context, cleanupInvoked, DynamicApiTerminationReason.ERROR, ex);
                    }
                    if (!resultFuture.isDone()) {
                        String traceId = MDC.get("traceId");
                        boolean interrupted = ex instanceof InterruptedException
                                || ex instanceof CancellationException
                                || Thread.currentThread().isInterrupted();
                        if (interrupted) {
                            log.debug("Dynamic api execution interrupted: apiId={}, path={}, method={}, type={}, traceId={}",
                                    context.getMeta().getApi().getId(),
                                    context.getMeta().getApi().getPath(),
                                    context.getMeta().getApi().getMethod(),
                                    context.getMeta().getType(),
                                    traceId);
                            reason = DynamicApiTerminationReason.TIMEOUT;
                            resultFuture.complete(DynamicApiExecuteResult.error(constants.getController().getServiceUnavailableCode(),
                                    constants.getMessage().getTimeout(),
                                    DynamicApiTerminationReason.TIMEOUT));
                        } else {
                            log.error("Dynamic api execute failed: apiId={}, path={}, method={}, type={}, traceId={}",
                                    context.getMeta().getApi().getId(),
                                    context.getMeta().getApi().getPath(),
                                    context.getMeta().getApi().getMethod(),
                                    context.getMeta().getType(),
                                    traceId,
                                    ex);
                            reason = DynamicApiTerminationReason.ERROR;
                            resultFuture.complete(DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                                    constants.getMessage().getExecuteFailed(),
                                    DynamicApiTerminationReason.ERROR));
                        }
                    }
                } finally {
                    if (completionRecorded.compareAndSet(false, true)) {
                        long end = System.nanoTime();
                        long execMs = startNanos.get() == 0L ? 0L : toMillis(end - startNanos.get());
                        long totalMs = toMillis(end - submitNanos);
                        if (metrics != null) {
                            metrics.recordComplete(context.getMeta(), success, reason, execMs, totalMs);
                        }
                        if (circuitBreaker != null) {
                            circuitBreaker.record(context.getMeta(), success, reason);
                        }
                    }
                }
            });
        } catch (RejectedExecutionException ex) {
            log.warn("Dynamic api execution rejected: apiId={}, path={}, method={}, type={}",
                    context.getMeta().getApi().getId(),
                    context.getMeta().getApi().getPath(),
                    context.getMeta().getApi().getMethod(),
                    context.getMeta().getType());
            if (metrics != null) {
                metrics.recordRejectAfterSubmit(context.getMeta(), DynamicApiTerminationReason.REJECTED);
            }
            return new DynamicApiExecution(
                    CompletableFuture.completedFuture(
                            DynamicApiExecuteResult.error(constants.getController().getRejectedCode(),
                                    constants.getMessage().getRejected(),
                                    DynamicApiTerminationReason.REJECTED)),
                    null);
        }
        Future<?> finalTask = taskFuture;
        return new DynamicApiExecution(resultFuture, (reason, cause) -> {
            if (resultFuture.isDone()) {
                return;
            }
            if (reason == DynamicApiTerminationReason.TIMEOUT) {
                invokeCleanupOnce(strategy, context, cleanupInvoked, reason, cause);
                if (finalTask != null) {
                    finalTask.cancel(true);
                }
                resultFuture.complete(DynamicApiExecuteResult.error(constants.getController().getServiceUnavailableCode(),
                        constants.getMessage().getTimeout(),
                        DynamicApiTerminationReason.TIMEOUT));
                if (completionRecorded.compareAndSet(false, true)) {
                    long end = System.nanoTime();
                    long execMs = startNanos.get() == 0L ? 0L : toMillis(end - startNanos.get());
                    long totalMs = toMillis(end - submitNanos);
                    if (metrics != null) {
                        metrics.recordComplete(context.getMeta(), false, DynamicApiTerminationReason.TIMEOUT, execMs, totalMs);
                    }
                    if (circuitBreaker != null) {
                        circuitBreaker.record(context.getMeta(), false, DynamicApiTerminationReason.TIMEOUT);
                    }
                }
                return;
            }
            if (reason == DynamicApiTerminationReason.ERROR) {
                invokeCleanupOnce(strategy, context, cleanupInvoked, reason, cause);
                if (completionRecorded.compareAndSet(false, true)) {
                    long end = System.nanoTime();
                    long execMs = startNanos.get() == 0L ? 0L : toMillis(end - startNanos.get());
                    long totalMs = toMillis(end - submitNanos);
                    if (metrics != null) {
                        metrics.recordComplete(context.getMeta(), false, DynamicApiTerminationReason.ERROR, execMs, totalMs);
                    }
                    if (circuitBreaker != null) {
                        circuitBreaker.record(context.getMeta(), false, DynamicApiTerminationReason.ERROR);
                    }
                }
                return;
            }
            if (reason == DynamicApiTerminationReason.CANCELLED) {
                invokeCleanupOnce(strategy, context, cleanupInvoked, reason, cause);
                if (finalTask != null) {
                    finalTask.cancel(true);
                }
                resultFuture.complete(DynamicApiExecuteResult.error(constants.getController().getRejectedCode(),
                        constants.getMessage().getRejected(),
                        DynamicApiTerminationReason.CANCELLED));
                if (completionRecorded.compareAndSet(false, true)) {
                    long end = System.nanoTime();
                    long execMs = startNanos.get() == 0L ? 0L : toMillis(end - startNanos.get());
                    long totalMs = toMillis(end - submitNanos);
                    if (metrics != null) {
                        metrics.recordComplete(context.getMeta(), false, DynamicApiTerminationReason.CANCELLED, execMs, totalMs);
                    }
                    if (circuitBreaker != null) {
                        circuitBreaker.record(context.getMeta(), false, DynamicApiTerminationReason.CANCELLED);
                    }
                }
            }
        });
    }

    private void invokeCleanupOnce(ExecuteStrategy strategy,
                                   DynamicApiContext context,
                                   AtomicBoolean guard,
                                   DynamicApiTerminationReason reason,
                                   Throwable cause) {
        if (strategy == null || guard == null || !guard.compareAndSet(false, true)) {
            return;
        }
        long timeoutMs = Math.max(1L, constants.getExecute().getCleanupTimeoutMs());
        Runnable cleanupTask = () -> {
            try {
                if (reason == DynamicApiTerminationReason.TIMEOUT) {
                    strategy.onTimeout(context);
                    return;
                }
                if (reason == DynamicApiTerminationReason.ERROR) {
                    strategy.onError(context, cause);
                    return;
                }
                if (reason == DynamicApiTerminationReason.CANCELLED) {
                    strategy.onCancel(context, cause);
                }
            } catch (Throwable ex) {
                log.warn("Dynamic api cleanup failed: apiId={}, type={}, reason={}, error={}",
                        context == null || context.getMeta() == null || context.getMeta().getApi() == null
                                ? null : context.getMeta().getApi().getId(),
                        context == null || context.getMeta() == null ? null : context.getMeta().getType(),
                        reason,
                        ex.getMessage());
            }
        };
        try {
            Future<?> future = cleanupExecutor.submit(cleanupTask);
            if (timeoutMs > 0) {
                cleanupScheduler.schedule(() -> {
                    if (future.isDone()) {
                        return;
                    }
                    future.cancel(true);
                    log.warn("Dynamic api cleanup timed out: apiId={}, type={}, reason={}",
                            context == null || context.getMeta() == null || context.getMeta().getApi() == null
                                    ? null : context.getMeta().getApi().getId(),
                            context == null || context.getMeta() == null ? null : context.getMeta().getType(),
                            reason);
                }, timeoutMs, TimeUnit.MILLISECONDS);
            }
        } catch (RejectedExecutionException ex) {
            log.warn("Dynamic api cleanup rejected: apiId={}, type={}, reason={}",
                    context == null || context.getMeta() == null || context.getMeta().getApi() == null
                            ? null : context.getMeta().getApi().getId(),
                    context == null || context.getMeta() == null ? null : context.getMeta().getType(),
                    reason);
        }
    }

    private long toMillis(long nanos) {
        return Math.max(0L, TimeUnit.NANOSECONDS.toMillis(nanos));
    }

    private DynamicApiTerminationReason resolveReason(DynamicApiExecuteResult result) {
        if (result == null) {
            return DynamicApiTerminationReason.ERROR;
        }
        if (result.isSuccess()) {
            return null;
        }
        String reason = result.getTerminationReason();
        if (reason == null || reason.trim().isEmpty()) {
            return DynamicApiTerminationReason.ERROR;
        }
        try {
            return DynamicApiTerminationReason.valueOf(reason);
        } catch (Exception ex) {
            return DynamicApiTerminationReason.ERROR;
        }
    }
}
