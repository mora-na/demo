package com.example.demo.post.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 岗位实体，映射 sys_post 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system.sys_post")
public class SysPost extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 岗位名称。
     */
    @TableField("name")
    private String name;

    /**
     * 岗位编码（唯一）。
     */
    @TableField("code")
    private String code;

    /**
     * 所属部门 ID。
     */
    @TableField("dept_id")
    private Long deptId;

    /**
     * 状态：1-启用，0-禁用。
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序。
     */
    @TableField("sort")
    private Integer sort;
}
