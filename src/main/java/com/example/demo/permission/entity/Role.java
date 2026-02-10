package com.example.demo.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
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
@TableName(value = "sys_role")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键 ID，用于唯一标识角色记录。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色编码（全局唯一）。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("code")
    private String code;

    /**
     * 角色名称（展示用）。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("name")
    private String name;

    /**
     * 状态标识：1-启用；0-禁用。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("status")
    private Integer status;

    /**
     * 数据范围类型：ALL/DEPT_AND_CHILD/DEPT/CUSTOM_DEPT/SELF/NONE。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("data_scope_type")
    private String dataScopeType;

    /**
     * 数据范围值，CUSTOM_DEPT 时存储部门 ID 列表。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("data_scope_value")
    private String dataScopeValue;
}
