package com.example.demo.datascope.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户数据范围覆盖实体，映射 sys_user_data_scope 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user_data_scope")
public class UserDataScope extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID。
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 数据范围标识（通常为菜单权限标识）。
     */
    @TableField("scope_key")
    private String scopeKey;

    /**
     * 数据范围类型。
     */
    @TableField("data_scope_type")
    private String dataScopeType;

    /**
     * 数据范围值（自定义部门 ID 列表）。
     */
    @TableField("data_scope_value")
    private String dataScopeValue;

    /**
     * 状态：1-启用，0-禁用。
     */
    @TableField("status")
    private Integer status;
}
