package com.example.demo.identity.api.dto;

import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * 身份域对外暴露的数据范围画像。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class IdentityDataScopeProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Long> deptTreeIds = new LinkedHashSet<>();
    private List<RoleDataScope> roleDataScopes;
    private Map<String, UserScopeOverride> userScopeOverrides = new LinkedHashMap<>();
}
