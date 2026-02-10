package com.example.demo.menu.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
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
@TableName(value = "sys_role_menu")
public class RoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键 ID，用于唯一标识关联记录。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
}
