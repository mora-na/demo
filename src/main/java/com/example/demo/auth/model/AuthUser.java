package com.example.demo.auth.model;

import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证上下文中的用户摘要信息，承载鉴权与数据范围字段。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private String userName;
    private String nickName;
    private Long deptId;
    private String deptName;
    private String dataScopeType;
    private String dataScopeValue;

    /**
     * 用户所属部门及子部门集合（登录时预计算）。
     */
    @JsonIgnore
    private java.util.Set<Long> deptTreeIds;

    /**
     * 角色数据范围集合（登录时预加载）。
     */
    @JsonIgnore
    private java.util.List<RoleDataScope> roleDataScopes;

    /**
     * 用户级数据范围覆盖（登录时预加载），key=scopeKey。
     */
    @JsonIgnore
    private java.util.Map<String, UserScopeOverride> userScopeOverrides;
}
