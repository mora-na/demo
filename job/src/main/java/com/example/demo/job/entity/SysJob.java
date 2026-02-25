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
 * 定时任务配置实体，映射 sys_job 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "demo_job.sys_job")
public class SysJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    private String name;

    @TableField("handler_name")
    private String handlerName;

    @TableField("cron_expression")
    private String cronExpression;

    @TableField("status")
    private Integer status;

    @TableField("allow_concurrent")
    private Integer allowConcurrent;

    @TableField("misfire_policy")
    private String misfirePolicy;

    @TableField("params")
    private String params;

    @TableField("log_collect_level")
    private String logCollectLevel;

    @TableField("remark")
    private String remark;

    @TableField("created_by")
    private Long createdBy;

    @TableField("created_name")
    private String createdName;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
