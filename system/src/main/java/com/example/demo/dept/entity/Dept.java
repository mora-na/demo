package com.example.demo.dept.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 部门实体，映射 sys_dept 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_dept")
@EqualsAndHashCode(callSuper = true)
public class Dept extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 部门名称。
     */
    @TableField("name")
    private String name;

    /**
     * 部门编码（唯一）。
     */
    @TableField("code")
    private String code;

    /**
     * 上级部门 ID。
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 状态：1-启用；0-禁用。
     */
    @TableField("status")
    private Integer status;

    /**
     * 排序值，数值越小越靠前。
     */
    @TableField("sort")
    private Integer sort;

}
