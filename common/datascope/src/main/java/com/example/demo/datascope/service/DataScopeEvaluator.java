package com.example.demo.datascope.service;

import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.mybatis.DataScopeType;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数据范围计算器，根据用户画像与 scopeKey 解析最终可见范围。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Component
@RequiredArgsConstructor
public class DataScopeEvaluator {

    private static final String GLOBAL_SCOPE_KEY = "GLOBAL";
    private static final int DISABLED_STATUS = 0;
    private final PermissionProperties permissionProperties;

    public FinalScope resolve(AuthUser user, String scopeKey) {
        if (user == null || user.getId() == null) {
            return FinalScope.none();
        }
        if (isSuperUser(user)) {
            return FinalScope.all();
        }
        String normalizedKey = StringUtils.isBlank(scopeKey) ? null : scopeKey.trim();
        UserScopeOverride override = resolveUserOverride(user, normalizedKey);
        if (override != null) {
            return applyOverride(user, override);
        }
        List<RoleDataScope> roles = user.getRoleDataScopes();
        if (roles == null || roles.isEmpty()) {
            return FinalScope.selfOnly(user.getId());
        }
        return mergeRoleScopes(user, normalizedKey, roles);
    }

    private UserScopeOverride resolveUserOverride(AuthUser user, String scopeKey) {
        Map<String, UserScopeOverride> overrides = user.getUserScopeOverrides();
        if (overrides == null || overrides.isEmpty()) {
            return null;
        }
        UserScopeOverride override = scopeKey == null ? null : overrides.get(scopeKey);
        if (override == null) {
            override = overrides.get(GLOBAL_SCOPE_KEY);
        }
        if (override == null) {
            return null;
        }
        if (override.getStatus() != null && override.getStatus() == DISABLED_STATUS) {
            return null;
        }
        return override;
    }

    private FinalScope applyOverride(AuthUser user, UserScopeOverride override) {
        String type = normalizeType(override.getDataScopeType());
        if (DataScopeType.ALL.equals(type)) {
            return FinalScope.all();
        }
        if (DataScopeType.SELF.equals(type)) {
            return FinalScope.selfOnly(user.getId());
        }
        if (DataScopeType.NONE.equals(type)) {
            return FinalScope.none();
        }
        if (DataScopeType.DEPT.equals(type)) {
            return FinalScope.ofDept(user.getDeptId());
        }
        if (DataScopeType.DEPT_AND_CHILD.equals(type)) {
            return FinalScope.ofDeptIds(user.getDeptTreeIds());
        }
        if (DataScopeType.CUSTOM_DEPT.equals(type) || DataScopeType.CUSTOM.equals(type)) {
            return FinalScope.ofDeptIds(override.getCustomDeptIds());
        }
        return FinalScope.selfOnly(user.getId());
    }

    private FinalScope mergeRoleScopes(AuthUser user, String scopeKey, List<RoleDataScope> roles) {
        boolean hasAny = false;
        boolean selfOnly = false;
        LinkedHashSet<Long> deptIds = new LinkedHashSet<>();
        for (RoleDataScope roleScope : roles) {
            if (roleScope == null) {
                continue;
            }
            RoleScope selection = resolveRoleScope(roleScope, scopeKey);
            if (selection == null) {
                continue;
            }
            hasAny = true;
            if (DataScopeType.ALL.equals(selection.type)) {
                return FinalScope.all();
            }
            if (DataScopeType.SELF.equals(selection.type)) {
                selfOnly = true;
                continue;
            }
            if (DataScopeType.NONE.equals(selection.type)) {
                continue;
            }
            if (DataScopeType.DEPT.equals(selection.type)) {
                if (user.getDeptId() != null) {
                    deptIds.add(user.getDeptId());
                }
                continue;
            }
            if (DataScopeType.DEPT_AND_CHILD.equals(selection.type)) {
                if (user.getDeptTreeIds() != null) {
                    deptIds.addAll(user.getDeptTreeIds());
                } else if (user.getDeptId() != null) {
                    deptIds.add(user.getDeptId());
                }
                continue;
            }
            if (DataScopeType.CUSTOM_DEPT.equals(selection.type) || DataScopeType.CUSTOM.equals(selection.type)) {
                if (selection.customDeptIds != null) {
                    deptIds.addAll(selection.customDeptIds);
                }
            }
        }
        if (!deptIds.isEmpty() || selfOnly) {
            return new FinalScope(false, selfOnly, deptIds, false);
        }
        if (hasAny) {
            return FinalScope.none();
        }
        return FinalScope.selfOnly(user.getId());
    }

    private RoleScope resolveRoleScope(RoleDataScope roleScope, String scopeKey) {
        String type = null;
        Set<Long> customDeptIds = null;
        if (scopeKey != null && roleScope.getMenuDataScopes() != null) {
            type = roleScope.getMenuDataScopes().get(scopeKey);
            if (type != null) {
                customDeptIds = roleScope.getMenuCustomDepts() == null
                        ? null
                        : roleScope.getMenuCustomDepts().get(scopeKey);
            }
        }
        if (type == null) {
            type = roleScope.getDataScopeType();
            customDeptIds = roleScope.getCustomDeptIds();
        }
        type = normalizeType(type);
        if (type == null) {
            return null;
        }
        return new RoleScope(type, customDeptIds);
    }

    private String normalizeType(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String upper = value.trim().toUpperCase(Locale.ROOT);
        switch (upper) {
            case DataScopeType.ALL:
            case DataScopeType.DEPT:
            case DataScopeType.DEPT_AND_CHILD:
            case DataScopeType.CUSTOM_DEPT:
            case DataScopeType.CUSTOM:
            case DataScopeType.SELF:
            case DataScopeType.NONE:
                return upper;
            default:
                return null;
        }
    }

    private boolean isSuperUser(AuthUser user) {
        if (user == null) {
            return false;
        }
        if (permissionProperties == null) {
            return false;
        }
        List<String> superUsers = permissionProperties.getSuperUsers();
        if (superUsers == null || superUsers.isEmpty()) {
            return false;
        }
        String userName = user.getUserName();
        if (userName == null) {
            return false;
        }
        String normalized = userName.toLowerCase(Locale.ROOT);
        for (String superUser : superUsers) {
            if (superUser != null && normalized.equals(superUser.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private static final class RoleScope {
        private final String type;
        private final Set<Long> customDeptIds;

        private RoleScope(String type, Set<Long> customDeptIds) {
            this.type = type;
            this.customDeptIds = customDeptIds;
        }
    }

    @Getter
    public static final class FinalScope {
        private final boolean all;
        private final boolean self;
        private final Set<Long> deptIds;
        private final boolean none;

        private FinalScope(boolean all, boolean self, Set<Long> deptIds, boolean none) {
            this.all = all;
            this.self = self;
            this.deptIds = deptIds == null ? Collections.emptySet() : deptIds;
            this.none = none;
        }

        public static FinalScope all() {
            return new FinalScope(true, false, Collections.emptySet(), false);
        }

        public static FinalScope none() {
            return new FinalScope(false, false, Collections.emptySet(), true);
        }

        public static FinalScope selfOnly(Long userId) {
            if (userId == null) {
                return none();
            }
            return new FinalScope(false, true, Collections.emptySet(), false);
        }

        public static FinalScope ofDept(Long deptId) {
            if (deptId == null) {
                return none();
            }
            return new FinalScope(false, false, new LinkedHashSet<>(Collections.singletonList(deptId)), false);
        }

        public static FinalScope ofDeptIds(Set<Long> deptIds) {
            if (deptIds == null || deptIds.isEmpty()) {
                return none();
            }
            return new FinalScope(false, false, new LinkedHashSet<>(deptIds), false);
        }

        public static FinalScope ofDeptIdsAndSelf(Set<Long> deptIds, boolean includeSelf) {
            if ((deptIds == null || deptIds.isEmpty()) && !includeSelf) {
                return none();
            }
            return new FinalScope(false, includeSelf, deptIds == null ? Collections.emptySet() : new LinkedHashSet<>(deptIds), false);
        }

    }
}
