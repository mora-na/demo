package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

/**
 * 动态接口执行句柄。
 */
public class DynamicApiExecution {

    private final CompletableFuture<DynamicApiExecuteResult> future;
    private final BiConsumer<DynamicApiTerminationReason, Throwable> cancelHook;

    public DynamicApiExecution(CompletableFuture<DynamicApiExecuteResult> future,
                               BiConsumer<DynamicApiTerminationReason, Throwable> cancelHook) {
        this.future = future;
        this.cancelHook = cancelHook;
    }

    public CompletableFuture<DynamicApiExecuteResult> getFuture() {
        return future;
    }

    public void cancelTimeout() {
        cancel(DynamicApiTerminationReason.TIMEOUT, null);
    }

    public void cancel(DynamicApiTerminationReason reason, Throwable cause) {
        if (cancelHook == null) {
            return;
        }
        cancelHook.accept(reason, cause);
    }
}
