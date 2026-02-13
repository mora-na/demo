package com.example.demo.job.log;

/**
 * 任务日志线程上下文（InheritableThreadLocal），用于无 MDC 透传场景。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public final class JobLogThreadContext {

    private static final InheritableThreadLocal<String> RUN_ID = new InheritableThreadLocal<>();

    private JobLogThreadContext() {
    }

    public static void set(String runId) {
        if (runId == null || runId.trim().isEmpty()) {
            return;
        }
        RUN_ID.set(runId);
    }

    public static String get() {
        return RUN_ID.get();
    }

    public static void clear() {
        RUN_ID.remove();
    }
}
