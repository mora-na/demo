package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DynamicApiExecutorCallbackTest {

    private static ThreadPoolTaskExecutor buildExecutor(String prefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix(prefix);
        executor.initialize();
        return executor;
    }

    @Test
    void successDoesNotInvokeCleanupCallbacks() throws Exception {
        RecordingStrategy strategy = new RecordingStrategy(Behavior.SUCCESS);
        try (ExecutorHarness harness = new ExecutorHarness(strategy)) {
            DynamicApiExecution execution = harness.executor.submit(buildContext());
            DynamicApiExecuteResult result = execution.getFuture().get(2, TimeUnit.SECONDS);
            assertTrue(result.isSuccess());
            assertTrue(strategy.beforeExecute.await(1, TimeUnit.SECONDS));
            assertTrue(strategy.afterExecute.await(1, TimeUnit.SECONDS));
            assertFalse(strategy.onTimeout.await(100, TimeUnit.MILLISECONDS));
            assertFalse(strategy.onError.await(100, TimeUnit.MILLISECONDS));
            assertFalse(strategy.onCancel.await(100, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    void errorInvokesOnErrorCallback() throws Exception {
        RecordingStrategy strategy = new RecordingStrategy(Behavior.ERROR);
        try (ExecutorHarness harness = new ExecutorHarness(strategy)) {
            DynamicApiExecution execution = harness.executor.submit(buildContext());
            DynamicApiExecuteResult result = execution.getFuture().get(2, TimeUnit.SECONDS);
            assertFalse(result.isSuccess());
            assertTrue(strategy.beforeExecute.await(1, TimeUnit.SECONDS));
            assertTrue(strategy.afterExecute.await(1, TimeUnit.SECONDS));
            assertTrue(strategy.onError.await(1, TimeUnit.SECONDS));
            assertFalse(strategy.onTimeout.await(100, TimeUnit.MILLISECONDS));
            assertFalse(strategy.onCancel.await(100, TimeUnit.MILLISECONDS));
        }
    }

    @Test
    void timeoutInvokesOnTimeoutCallback() throws Exception {
        RecordingStrategy strategy = new RecordingStrategy(Behavior.BLOCK);
        try (ExecutorHarness harness = new ExecutorHarness(strategy)) {
            DynamicApiExecution execution = harness.executor.submit(buildContext());
            assertTrue(strategy.started.await(1, TimeUnit.SECONDS));
            execution.cancelTimeout();
            DynamicApiExecuteResult result = execution.getFuture().get(2, TimeUnit.SECONDS);
            assertEquals(DynamicApiTerminationReason.TIMEOUT.name(), result.getTerminationReason());
            assertTrue(strategy.onTimeout.await(1, TimeUnit.SECONDS));
            assertTrue(strategy.afterExecute.await(1, TimeUnit.SECONDS));
            assertFalse(strategy.onError.await(100, TimeUnit.MILLISECONDS));
            assertFalse(strategy.onCancel.await(100, TimeUnit.MILLISECONDS));
        } finally {
            strategy.releaseBlock();
        }
    }

    @Test
    void cancelInvokesOnCancelCallback() throws Exception {
        RecordingStrategy strategy = new RecordingStrategy(Behavior.BLOCK);
        try (ExecutorHarness harness = new ExecutorHarness(strategy)) {
            DynamicApiExecution execution = harness.executor.submit(buildContext());
            assertTrue(strategy.started.await(1, TimeUnit.SECONDS));
            execution.cancel(DynamicApiTerminationReason.CANCELLED, new RuntimeException("cancel"));
            DynamicApiExecuteResult result = execution.getFuture().get(2, TimeUnit.SECONDS);
            assertEquals(DynamicApiTerminationReason.CANCELLED.name(), result.getTerminationReason());
            assertTrue(strategy.onCancel.await(1, TimeUnit.SECONDS));
            assertTrue(strategy.afterExecute.await(1, TimeUnit.SECONDS));
            assertFalse(strategy.onTimeout.await(100, TimeUnit.MILLISECONDS));
            assertFalse(strategy.onError.await(100, TimeUnit.MILLISECONDS));
        } finally {
            strategy.releaseBlock();
        }
    }

    @Test
    void cancelBeforeStartStillInvokesAfterExecute() throws Exception {
        RecordingStrategy strategy = new RecordingStrategy(Behavior.BLOCK);
        try (ExecutorHarness harness = new ExecutorHarness(strategy)) {
            DynamicApiExecution first = harness.executor.submit(buildContext());
            assertTrue(strategy.started.await(1, TimeUnit.SECONDS));
            DynamicApiExecution queued = harness.executor.submit(buildContext());
            queued.cancel(DynamicApiTerminationReason.CANCELLED, new RuntimeException("cancel"));
            DynamicApiExecuteResult result = queued.getFuture().get(2, TimeUnit.SECONDS);
            assertEquals(DynamicApiTerminationReason.CANCELLED.name(), result.getTerminationReason());
            assertEquals(1, strategy.executeCount.get());
            assertTrue(strategy.onCancel.await(1, TimeUnit.SECONDS));
            assertTrue(strategy.afterExecute.await(1, TimeUnit.SECONDS));
            first.getFuture().cancel(true);
        } finally {
            strategy.releaseBlock();
        }
    }

    private DynamicApiContext buildContext() {
        DynamicApi api = new DynamicApi();
        api.setId(1L);
        api.setPath("/ext/test");
        api.setMethod("GET");
        api.setType("TEST");
        DynamicApiMeta meta = new DynamicApiMeta(api, "TEST", DynamicApiAuthMode.INHERIT, null, null);
        DynamicApiRequest request = DynamicApiRequest.builder()
                .path("/ext/test")
                .method("GET")
                .build();
        return new DynamicApiContext(meta, request, 1000L, null, null, null, null);
    }

    private enum Behavior {
        SUCCESS,
        ERROR,
        BLOCK
    }

    private static class ExecutorHarness implements AutoCloseable {
        private final ThreadPoolTaskExecutor taskExecutor;
        private final ThreadPoolTaskExecutor cleanupExecutor;
        private final ScheduledExecutorService cleanupScheduler;
        private final DynamicApiExecutor executor;

        private ExecutorHarness(ExecuteStrategy strategy) {
            this.taskExecutor = buildExecutor("ext-test-");
            this.cleanupExecutor = buildExecutor("ext-cleanup-test-");
            this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("ext-cleanup-scheduler-test");
                return thread;
            });
            DynamicApiConstants constants = new DynamicApiConstants();
            DynamicApiProperties properties = new DynamicApiProperties();
            ExecuteStrategyFactory factory = new ExecuteStrategyFactory(Collections.singletonList(strategy), properties);
            DynamicApiExecutorRouter router = new DynamicApiExecutorRouter(taskExecutor, Collections.emptyMap(), properties);
            this.executor = new DynamicApiExecutor(taskExecutor, cleanupExecutor, cleanupScheduler,
                    factory, constants, router, null, null);
        }

        @Override
        public void close() {
            taskExecutor.shutdown();
            cleanupExecutor.shutdown();
            cleanupScheduler.shutdownNow();
        }
    }

    private static class RecordingStrategy implements ExecuteStrategy {
        private final Behavior behavior;
        private final CountDownLatch started = new CountDownLatch(1);
        private final CountDownLatch blockLatch = new CountDownLatch(1);
        private final CountDownLatch onTimeout = new CountDownLatch(1);
        private final CountDownLatch onError = new CountDownLatch(1);
        private final CountDownLatch onCancel = new CountDownLatch(1);
        private final CountDownLatch beforeExecute = new CountDownLatch(1);
        private final CountDownLatch afterExecute = new CountDownLatch(1);
        private final AtomicInteger executeCount = new AtomicInteger(0);

        private RecordingStrategy(Behavior behavior) {
            this.behavior = behavior;
        }

        @Override
        public String type() {
            return "TEST";
        }

        @Override
        public void beforeExecute(DynamicApiExecutionContext context) {
            beforeExecute.countDown();
        }

        @Override
        public DynamicApiExecuteResult execute(DynamicApiExecutionContext context) throws Exception {
            executeCount.incrementAndGet();
            started.countDown();
            switch (behavior) {
                case SUCCESS:
                    return DynamicApiExecuteResult.success("ok");
                case ERROR:
                    throw new RuntimeException("boom");
                case BLOCK:
                    blockLatch.await();
                    return DynamicApiExecuteResult.success("ok");
                default:
                    return DynamicApiExecuteResult.success("ok");
            }
        }

        @Override
        public void onTimeout(DynamicApiExecutionContext context) {
            onTimeout.countDown();
        }

        @Override
        public void onError(DynamicApiExecutionContext context, Throwable error) {
            onError.countDown();
        }

        @Override
        public void onCancel(DynamicApiExecutionContext context, Throwable cause) {
            onCancel.countDown();
        }

        @Override
        public void afterExecute(DynamicApiExecutionContext context, DynamicApiExecuteResult result, Throwable error) {
            afterExecute.countDown();
        }

        private void releaseBlock() {
            blockLatch.countDown();
        }
    }
}
