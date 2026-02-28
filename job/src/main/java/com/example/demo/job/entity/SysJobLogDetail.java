package com.example.demo.job.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行明细日志实体，映射 sys_job_log_detail 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "demo_job.sys_job_log_detail")
public class SysJobLogDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("job_log_id")
    private Long jobLogId;

    @TableField("log_level")
    private String logLevel;

    @TableField("log_start_time")
    private LocalDateTime logStartTime;

    @TableField("log_end_time")
    private LocalDateTime logEndTime;

    @TableField("log_content")
    private String logContent;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
