package com.example.demo.job.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 定时任务视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class JobVO {

    private Long id;

    private String name;

    private String handlerName;

    private String cronExpression;

    private Integer status;

    private Integer allowConcurrent;

    private String misfirePolicy;

    private String params;

    private String logCollectLevel;

    private String remark;

    private String createdName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime nextFireTime;
}
