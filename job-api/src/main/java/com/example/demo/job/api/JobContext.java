package com.example.demo.job.api;

import lombok.Data;

/**
 * Job execution context.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class JobContext {

    private final StringBuilder logBuffer = new StringBuilder();
    private Long jobId;
    private String jobName;
    private String handlerName;
    private String cronExpression;
    private String params;
    private int maxLogLength;

    public void setMaxLogLength(int maxLogLength) {
        this.maxLogLength = Math.max(0, maxLogLength);
    }

    public void appendLog(String message) {
        if (message == null) {
            return;
        }
        String value = message.trim();
        if (value.isEmpty()) {
            return;
        }
        if (maxLogLength > 0 && logBuffer.length() >= maxLogLength) {
            return;
        }
        int remaining = maxLogLength > 0 ? maxLogLength - logBuffer.length() : Integer.MAX_VALUE;
        if (logBuffer.length() > 0) {
            if (remaining <= 1) {
                return;
            }
            logBuffer.append('\n');
            remaining -= 1;
        }
        if (value.length() > remaining) {
            logBuffer.append(value, 0, remaining);
            return;
        }
        logBuffer.append(value);
    }

    public String getLogContent() {
        return logBuffer.toString();
    }
}
