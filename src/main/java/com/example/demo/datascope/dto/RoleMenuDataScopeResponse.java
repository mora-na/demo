package com.example.demo.datascope.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色菜单级数据范围配置响应。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class RoleMenuDataScopeResponse {

    private Long roleId;
    private String roleCode;
    private String roleName;
    private String defaultDataScopeType;
    private String defaultDataScopeValue;
    private List<RoleMenuDataScopeItemVO> items;
}
