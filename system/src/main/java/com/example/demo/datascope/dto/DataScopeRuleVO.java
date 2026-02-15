package com.example.demo.datascope.dto;

import lombok.Data;

/**
 * 数据范围字段映射视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class DataScopeRuleVO {

    private Long id;
    private String scopeKey;
    private String tableName;
    private String tableAlias;
    private String deptColumn;
    private String userColumn;
    private Integer filterType;
    private Integer status;
    private String remark;
}
