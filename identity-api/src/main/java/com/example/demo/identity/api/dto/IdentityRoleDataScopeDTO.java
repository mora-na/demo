package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 角色数据范围配置 DTO（身份域对外契约）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
@Data
public class IdentityRoleDataScopeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long roleId;

    private String roleCode;

    private String dataScopeType;

    private Set<Long> customDeptIds = new LinkedHashSet<>();

    private Map<String, String> menuDataScopes = new LinkedHashMap<>();

    private Map<String, Set<Long>> menuCustomDepts = new LinkedHashMap<>();
}
