package com.example.demo.datascope.dto;

import lombok.Data;

/**
 * 数据范围字段映射查询条件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class DataScopeRuleQuery {

    private String scopeKey;
    private String tableName;
}
