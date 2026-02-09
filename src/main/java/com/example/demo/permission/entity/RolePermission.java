package com.example.demo.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.annotation.MppMultiField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色-权限关联实体，映射 sys_role_permission 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_role_permission")
public class RolePermission implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键 ID，用于唯一标识关联记录。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色 ID，联合字段之一。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @MppMultiField
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限 ID，联合字段之一。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @MppMultiField
    @TableField("permission_id")
    private Long permissionId;
}
