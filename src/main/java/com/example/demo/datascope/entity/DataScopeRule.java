package com.example.demo.datascope.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
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
@TableName(value = "sys_data_scope_rule")
public class DataScopeRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键 ID，用于唯一标识规则记录。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标表名（小写匹配）。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 数据范围字段名（用于权限过滤）。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 是否启用：1-启用；0-禁用。
     *
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @TableField("enabled")
    private Integer enabled;
}
