package com.example.demo.job.support;

import lombok.Data;


/**
 * 定时任务执行上下文。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class JobContext {

    private Long jobId;

    private String jobName;

    private String handlerName;

    private String cronExpression;

    private String params;

    private final StringBuilder logBuffer = new StringBuilder();

    public void appendLog(String message) {
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        if (logBuffer.length() > 0) {
            logBuffer.append('\n');
        }
        logBuffer.append(message);
    }

    public String getLogContent() {
        return logBuffer.toString();
    }
}
