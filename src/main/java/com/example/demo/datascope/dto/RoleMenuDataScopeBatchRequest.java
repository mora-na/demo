package com.example.demo.datascope.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色菜单级数据范围批量保存请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class RoleMenuDataScopeBatchRequest {

    @Valid
    private List<RoleMenuDataScopeItemRequest> items;
}
