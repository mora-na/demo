package com.example.demo.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行记录实体，映射 sys_job_log 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "demo_job.sys_job_log")
public class SysJobLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("job_id")
    private Long jobId;

    @TableField("job_name")
    private String jobName;

    @TableField("handler_name")
    private String handlerName;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("params")
    private String params;

    @TableField("trigger_type")
    private String triggerType;

    @TableField("trigger_user_id")
    private Long triggerUserId;

    @TableField("trigger_user_name")
    private String triggerUserName;

    @TableField("fire_time")
    private LocalDateTime fireTime;

    @TableField("scheduled_fire_time")
    private LocalDateTime scheduledFireTime;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("duration_ms")
    private Long durationMs;

    @TableField("status")
    private Integer status;

    @TableField("error_message")
    private String errorMessage;

    @TableField("error_stacktrace")
    private String errorStacktrace;

    @TableField("scheduler_instance")
    private String schedulerInstance;

    @TableField("fire_instance_id")
    private String fireInstanceId;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
