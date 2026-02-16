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
 * 数据范围规则实体，映射 sys_data_scope_rule 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system.sys_data_scope_rule")
public class DataScopeRule extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据范围标识（通常为菜单权限标识）。
     */
    @TableField("scope_key")
    private String scopeKey;

    /**
     * 目标表名。
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 表别名（可选）。
     */
    @TableField("table_alias")
    private String tableAlias;

    /**
     * 部门字段名。
     */
    @TableField("dept_column")
    private String deptColumn;

    /**
     * 用户字段名。
     */
    @TableField("user_column")
    private String userColumn;

    /**
     * 过滤方式：1=追加WHERE 2=EXISTS 3=JOIN。
     */
    @TableField("filter_type")
    private Integer filterType;

    /**
     * 状态：1-启用；0-禁用。
     */
    @TableField("status")
    private Integer status;
}
