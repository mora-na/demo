package com.example.demo.common.async;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * MDC 透传的 ScheduledExecutorService 包装器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public class MdcScheduledExecutorService extends MdcExecutorService implements ScheduledExecutorService {

    private final ScheduledExecutorService delegate;

    public MdcScheduledExecutorService(ScheduledExecutorService delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public ScheduledFuture<?> schedule(@NonNull Runnable command, long delay, @NonNull TimeUnit unit) {
        return delegate.schedule(MdcUtils.wrap(command), delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(@NonNull Callable<V> callable, long delay, @NonNull TimeUnit unit) {
        return delegate.schedule(MdcUtils.wrap(callable), delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(@NonNull Runnable command, long initialDelay, long period, @NonNull TimeUnit unit) {
        return delegate.scheduleAtFixedRate(MdcUtils.wrap(command), initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(@NonNull Runnable command, long initialDelay, long delay, @NonNull TimeUnit unit) {
        return delegate.scheduleWithFixedDelay(MdcUtils.wrap(command), initialDelay, delay, unit);
    }
}
