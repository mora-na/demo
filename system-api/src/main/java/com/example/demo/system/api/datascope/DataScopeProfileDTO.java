package com.example.demo.system.api.datascope;

import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * 登录用户数据范围画像。
 */
@Data
public class DataScopeProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Set<Long> deptTreeIds = new LinkedHashSet<>();
    private List<RoleDataScope> roleDataScopes = new ArrayList<>();
    private Map<String, UserScopeOverride> userScopeOverrides = new LinkedHashMap<>();
}
