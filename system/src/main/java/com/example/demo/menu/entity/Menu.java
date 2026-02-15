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
 * 菜单实体，映射 sys_menu 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_menu")
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 菜单名称。
     */
    @TableField("name")
    private String name;

    /**
     * 菜单编码（唯一）。
     */
    @TableField("code")
    private String code;

    /**
     * 上级菜单 ID。
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 路由路径。
     */
    @TableField("path")
    private String path;

    /**
     * 前端组件路径。
     */
    @TableField("component")
    private String component;

    /**
     * 菜单权限标识。
     */
    @TableField("permission")
    private String permission;

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
