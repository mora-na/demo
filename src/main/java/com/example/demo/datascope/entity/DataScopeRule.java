package com.example.demo.datascope.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_data_scope_rule")
public class DataScopeRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 目标表名（小写匹配）
     */
    @TableField("table_name")
    private String tableName;

    /**
     * 数据范围字段名
     */
    @TableField("column_name")
    private String columnName;

    /**
     * 是否启用：1-启用；0-禁用
     */
    @TableField("enabled")
    private Integer enabled;
}
