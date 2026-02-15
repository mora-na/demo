package com.example.demo.common.async;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * MDC 透传任务装饰器，用于异步/线程池执行时继承调用线程上下文。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        if (contextMap == null || contextMap.isEmpty()) {
            return runnable;
        }
        return () -> {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                MDC.setContextMap(contextMap);
                runnable.run();
            } finally {
                if (previous == null || previous.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previous);
                }
            }
        };
    }
}
