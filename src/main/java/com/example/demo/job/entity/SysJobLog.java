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
 * 定时任务执行日志实体，映射 sys_job_log 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_job_log")
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

    @TableField("status")
    private Integer status;

    @TableField("message")
    private String message;

    @TableField("log_detail")
    private String logDetail;

    @TableField("start_time")
    private LocalDateTime startTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("duration_ms")
    private Long durationMs;
}
