package com.example.demo.common.async;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * MDC 透传的 ExecutorService 包装器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public class MdcExecutorService extends AbstractExecutorService {

    private final ExecutorService delegate;

    public MdcExecutorService(ExecutorService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void shutdown() {
        delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        delegate.execute(MdcUtils.wrap(command));
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(MdcUtils.wrap(task));
    }

    @Override
    public Future<?> submit(Runnable task) {
        return delegate.submit(MdcUtils.wrap(task));
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(MdcUtils.wrap(task), result);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(wrapTasks(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return delegate.invokeAll(wrapTasks(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, java.util.concurrent.ExecutionException {
        return delegate.invokeAny(wrapTasks(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, java.util.concurrent.ExecutionException, java.util.concurrent.TimeoutException {
        return delegate.invokeAny(wrapTasks(tasks), timeout, unit);
    }

    private <T> Collection<? extends Callable<T>> wrapTasks(Collection<? extends Callable<T>> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return tasks;
        }
        List<Callable<T>> wrapped = new ArrayList<>(tasks.size());
        for (Callable<T> task : tasks) {
            wrapped.add(MdcUtils.wrap(task));
        }
        return wrapped;
    }
}
