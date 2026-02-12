package com.example.demo.permission.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
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
public class RolePermission extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限 ID。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("permission_id")
    private Long permissionId;
}
