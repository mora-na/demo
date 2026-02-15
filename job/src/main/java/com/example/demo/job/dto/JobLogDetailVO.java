package com.example.demo.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务日志详情视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class JobLogDetailVO {

    private Long id;

    private Long jobId;

    private String jobName;

    private String handlerName;

    private Integer status;

    private String message;

    private String logDetail;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMs;
}
