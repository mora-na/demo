package com.example.demo.datascope.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 数据范围字段映射更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class DataScopeRuleUpdateRequest {

    @Size(max = 200)
    private String scopeKey;

    @Size(max = 100)
    private String tableName;

    @Size(max = 20)
    private String tableAlias;

    @Size(max = 100)
    private String deptColumn;

    @Size(max = 100)
    private String userColumn;

    private Integer filterType;

    private Integer status;

    @Size(max = 500)
    private String remark;
}
