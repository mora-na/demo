package com.example.demo.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行记录视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Data
public class JobLogVO {

    private Long id;

    private Long jobId;

    private String jobName;

    private String handlerName;

    private String cronExpression;

    private String params;

    private String triggerType;

    private Long triggerUserId;

    private String triggerUserName;

    private LocalDateTime fireTime;

    private LocalDateTime scheduledFireTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;

    private Integer status;

    private String errorMessage;

    private String errorStacktrace;

    private String schedulerInstance;

    private String fireInstanceId;
}
