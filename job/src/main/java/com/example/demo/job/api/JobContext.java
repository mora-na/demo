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

    private Long jobId;
    private String jobName;
    private String handlerName;
    private String cronExpression;
    private String params;
    private Long executionLogId;
}
