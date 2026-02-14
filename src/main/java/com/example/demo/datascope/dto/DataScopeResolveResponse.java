package com.example.demo.datascope.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 数据范围解析响应结构。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class DataScopeResolveResponse {

    private UserSummary user;
    private MenuSummary menu;
    private Layer3Summary layer3;
    private List<RoleScopeSummary> roleScopes;
    private Set<Long> mergedDeptIds;
    private boolean includeSelf;
    private String finalScopeLabel;
    private RuleSummary rule;
    private String sqlCondition;

    @Data
    public static class UserSummary {
        private Long id;
        private String userName;
        private String nickName;
        private Long deptId;
        private String deptName;
        private List<String> posts;
        private List<RoleSummary> roles;
    }

    @Data
    public static class RoleSummary {
        private Long id;
        private String code;
        private String name;
        private String dataScopeType;
        private String dataScopeValue;
    }

    @Data
    public static class MenuSummary {
        private Long id;
        private String name;
        private String permission;
    }

    @Data
    public static class Layer3Summary {
        private String scopeKey;
        private String dataScopeType;
        private String dataScopeValue;
    }

    @Data
    public static class RoleScopeSummary {
        private Long roleId;
        private String roleCode;
        private String roleName;
        private String layer1Type;
        private String layer2Type;
        private String effectiveType;
        private String sourceLayer;
        private Set<Long> customDeptIds;
    }

    @Data
    public static class RuleSummary {
        private String source;
        private String tableName;
        private String tableAlias;
        private String deptColumn;
        private String userColumn;
    }
}
