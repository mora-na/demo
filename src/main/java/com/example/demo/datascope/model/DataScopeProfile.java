package com.example.demo.datascope.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * 登录用户数据范围画像，包含角色范围与用户覆盖配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Long> deptTreeIds = new LinkedHashSet<>();

    private List<RoleDataScope> roleDataScopes;

    private Map<String, UserScopeOverride> userScopeOverrides = new LinkedHashMap<>();
}
