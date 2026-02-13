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
 * 权限实体，映射 sys_permission 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_permission")
public class Permission extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 权限编码（全局唯一）。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("code")
    private String code;

    /**
     * 权限名称（展示用）。
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
}
