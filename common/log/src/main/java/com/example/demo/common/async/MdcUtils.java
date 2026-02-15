package com.example.demo.common.async;

import com.example.demo.common.config.CommonConstants;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MDC 透传工具，适用于手动线程与自建线程池。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public final class MdcUtils {

    private MdcUtils() {
    }

    public static Runnable wrap(Runnable task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (contextMap == null || contextMap.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(contextMap);
                }
                task.run();
            } finally {
                if (previous == null || previous.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }

    public static <T> Callable<T> wrap(Callable<T> task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                if (contextMap == null || contextMap.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(contextMap);
                }
                return task.call();
            } finally {
                if (previous == null || previous.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }

    public static Thread newThread(String name, Runnable task, boolean daemon) {
        Thread thread = new Thread(wrap(task));
        if (name != null && !name.trim().isEmpty()) {
            thread.setName(name);
        }
        thread.setDaemon(daemon);
        return thread;
    }

    public static ThreadFactory threadFactory(String namePrefix, boolean daemon) {
        String prefix = namePrefix;
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = CommonConstants.Mdc.DEFAULT_THREAD_NAME_PREFIX;
        }
        final String threadNamePrefix = prefix;
        AtomicInteger index = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(wrap(runnable));
            thread.setName(threadNamePrefix + index.incrementAndGet());
            thread.setDaemon(daemon);
            return thread;
        };
    }

    public static ExecutorService wrapExecutorService(ExecutorService delegate) {
        if (delegate == null || delegate instanceof MdcExecutorService) {
            return delegate;
        }
        return new MdcExecutorService(delegate);
    }

    public static ScheduledExecutorService wrapScheduledExecutorService(ScheduledExecutorService delegate) {
        if (delegate == null || delegate instanceof MdcScheduledExecutorService) {
            return delegate;
        }
        return new MdcScheduledExecutorService(delegate);
    }
}
