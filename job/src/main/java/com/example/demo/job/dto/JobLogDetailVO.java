package com.example.demo.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务执行明细日志视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Data
public class JobLogDetailVO {

    private Long id;

    private Long jobLogId;

    private String logLevel;

    private LocalDateTime logStartTime;

    private LocalDateTime logEndTime;

    private String logContent;
}
