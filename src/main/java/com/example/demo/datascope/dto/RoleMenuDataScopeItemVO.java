package com.example.demo.datascope.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色菜单级数据范围配置视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class RoleMenuDataScopeItemVO {

    private Long menuId;
    private String menuName;
    private Long parentId;
    private String permission;
    private String dataScopeType;
    private List<Long> customDeptIds;
}
