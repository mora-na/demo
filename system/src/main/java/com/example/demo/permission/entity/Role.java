package com.example.demo.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色实体，映射 sys_role 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system.sys_role")
public class Role extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色编码（全局唯一）。
     *
     * @date 2026/2/9
     */
    @TableField("code")
    private String code;

    /**
     * 角色名称（展示用）。
     *
     * @date 2026/2/9
     */
    @TableField("name")
    private String name;

    /**
     * 状态标识：1-启用；0-禁用。
     *
     * @date 2026/2/9
     */
    @TableField("status")
    private Integer status;

    /**
     * 数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE。
     *
     * @date 2026/2/9
     */
    @TableField("data_scope_type")
    private String dataScopeType;

    /**
     * 数据范围值，CUSTOM_DEPT 时存储部门 ID 列表。
     *
     * @date 2026/2/9
     */
    @TableField("data_scope_value")
    private String dataScopeValue;
}
