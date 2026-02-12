package com.example.demo.notice.entity;

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
 * 系统通知实体，映射 sys_notice 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_notice")
public class Notice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 通知标题。
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容。
     */
    @TableField("content")
    private String content;

    /**
     * 通知范围类型。
     */
    @TableField("scope_type")
    private String scopeType;

    /**
     * 通知范围值（ID 列表）。
     */
    @TableField("scope_value")
    private String scopeValue;

    /**
     * 创建人 ID。
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 创建人名称。
     */
    @TableField("created_name")
    private String createdName;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}
