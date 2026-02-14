package com.example.demo.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.example.demo.common.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体，提供通用审计与逻辑删除字段。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 创建人。
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人所属部门ID（数据归属部门）。
     */
    @TableField(value = "create_dept", fill = FieldFill.INSERT)
    private Long createDept;

    /**
     * 更新人。
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 逻辑删除标识：0-未删除；1-已删除。
     */
    @TableLogic
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 乐观锁版本号。
     */
    @Version
    @TableField("version")
    private Integer version;

    /**
     * 备注。
     */
    @TableField("remark")
    @Excel(header = "备注")
    private String remark;
}
