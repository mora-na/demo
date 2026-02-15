package com.example.demo.datascope.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 角色数据范围配置载体，承载默认范围与菜单级细化配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDataScope implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roleId;

    private String roleCode;

    /**
     * 角色默认数据范围类型（Layer1）。
     */
    private String dataScopeType;

    /**
     * 角色默认自定义部门集合（Layer1）。
     */
    private Set<Long> customDeptIds = new LinkedHashSet<>();

    /**
     * 菜单级数据范围类型映射（Layer2）。
     * key = scopeKey（通常为菜单权限标识）
     */
    private Map<String, String> menuDataScopes = new LinkedHashMap<>();

    /**
     * 菜单级自定义部门集合（Layer2）。
     * key = scopeKey（通常为菜单权限标识）
     */
    private Map<String, Set<Long>> menuCustomDepts = new LinkedHashMap<>();
}
