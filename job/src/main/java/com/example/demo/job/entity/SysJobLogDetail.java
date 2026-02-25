package com.example.demo.job.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务日志明细实体，映射 sys_job_log_detail 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/21
 */
@Data
@TableName(value = "demo_job.sys_job_log_detail")
public class SysJobLogDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("log_id")
    private Long logId;

    @TableField("part_type")
    private String partType;

    @TableField("log_detail")
    private String logDetail;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
