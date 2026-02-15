package com.example.demo.common.mybatis;

import lombok.Data;

/**
 * 数据范围规则定义（SDK 视角），用于描述 SQL 注入所需的最小规则信息。
 */
@Data
public class DataScopeRuleDefinition {

    private String scopeKey;

    private String tableName;

    private String tableAlias;

    private String deptColumn;

    private String userColumn;

    /**
     * 过滤方式：1=追加WHERE 2=EXISTS 3=JOIN。
     */
    private Integer filterType;

    /**
     * 状态：1-启用；0-禁用。
     */
    private Integer status;
}
