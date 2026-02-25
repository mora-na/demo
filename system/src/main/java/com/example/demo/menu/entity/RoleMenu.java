package com.example.demo.menu.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 角色-菜单关联实体，映射 sys_role_menu 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "demo_system.sys_role_menu")
@EqualsAndHashCode(callSuper = true)
public class RoleMenu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID。
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 菜单 ID。
     */
    @TableField("menu_id")
    private Long menuId;

    /**
     * 菜单级数据范围类型（Layer2）。
     */
    @TableField("data_scope_type")
    private String dataScopeType;
}
