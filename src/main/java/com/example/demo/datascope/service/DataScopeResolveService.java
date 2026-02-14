package com.example.demo.datascope.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.DataScopeRuleProvider;
import com.example.demo.common.mybatis.DataScopeType;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.datascope.dto.DataScopeResolveMenuVO;
import com.example.demo.datascope.dto.DataScopeResolveResponse;
import com.example.demo.datascope.entity.DataScopeRule;
import com.example.demo.datascope.model.DataScopeProfile;
import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.entity.RoleMenu;
import com.example.demo.menu.service.MenuService;
import com.example.demo.menu.service.RoleMenuService;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.service.RoleService;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.post.entity.SysPost;
import com.example.demo.post.entity.UserPost;
import com.example.demo.post.service.PostService;
import com.example.demo.post.service.UserPostService;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据范围解析服务，供权限总览页面使用。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Service
@RequiredArgsConstructor
public class DataScopeResolveService {

    private static final String GLOBAL_SCOPE_KEY = "*";
    private static final String SOURCE_LAYER_3 = "LAYER3";
    private static final String SOURCE_LAYER_2 = "LAYER2";
    private static final String SOURCE_LAYER_1 = "LAYER1";

    private final SysUserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final RoleMenuService roleMenuService;
    private final MenuService menuService;
    private final DeptService deptService;
    private final PostService postService;
    private final UserPostService userPostService;
    private final DataScopeProfileService dataScopeProfileService;
    private final DataScopeRuleProvider dataScopeRuleProvider;
    private final PermissionProperties permissionProperties;

    public DataScopeResolveResponse resolve(Long userId, String scopeKey) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            return null;
        }
        DataScopeProfile profile = dataScopeProfileService.buildProfile(user);
        return resolve(user, profile, scopeKey);
    }

    public List<DataScopeResolveMenuVO> resolveAll(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            return Collections.emptyList();
        }
        DataScopeProfile profile = dataScopeProfileService.buildProfile(user);
        List<Menu> menus = loadUserMenus(user.getId());
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }
        List<DataScopeResolveMenuVO> results = new ArrayList<>();
        for (Menu menu : menus) {
            if (menu == null || StringUtils.isBlank(menu.getPermission())) {
                continue;
            }
            DataScopeResolveResponse resolved = resolve(user, profile, menu.getPermission());
            DataScopeResolveMenuVO item = new DataScopeResolveMenuVO();
            item.setMenuId(menu.getId());
            item.setMenuName(menu.getName());
            item.setPermission(menu.getPermission());
            item.setFinalScopeLabel(resolved.getFinalScopeLabel());
            item.setSourceLayer(resolveSourceLayer(resolved.getLayer3(), resolved.getRoleScopes()));
            results.add(item);
        }
        return results;
    }

    private DataScopeResolveResponse resolve(SysUser user, DataScopeProfile profile, String scopeKey) {
        DataScopeResolveResponse response = new DataScopeResolveResponse();
        response.setUser(buildUserSummary(user, profile));
        response.setMenu(buildMenuSummary(scopeKey));

        if (isSuperUser(user)) {
            String normalizedKey = StringUtils.isBlank(scopeKey) ? null : scopeKey.trim();
            fillFinal(response, DataScopeEvaluator.FinalScope.all(), normalizedKey);
            return response;
        }

        String normalizedKey = StringUtils.isBlank(scopeKey) ? null : scopeKey.trim();
        UserScopeOverride override = resolveOverride(profile, normalizedKey);
        if (override != null) {
            response.setLayer3(buildLayer3Summary(override));
            DataScopeEvaluator.FinalScope finalScope = applyOverride(user, profile, override);
            fillFinal(response, finalScope, normalizedKey);
            return response;
        }

        List<RoleDataScope> roleScopes = profile.getRoleDataScopes();
        if (roleScopes == null) {
            roleScopes = Collections.emptyList();
        }
        RoleScopeResult merged = mergeRoleScopes(user, profile, roleScopes, normalizedKey);
        response.setRoleScopes(merged.details);
        response.setMergedDeptIds(merged.deptIds);
        response.setIncludeSelf(merged.includeSelf);
        response.setFinalScopeLabel(merged.finalLabel);
        fillRuleAndSql(response, merged.finalScope, normalizedKey);
        return response;
    }

    private DataScopeResolveResponse.UserSummary buildUserSummary(SysUser user, DataScopeProfile profile) {
        DataScopeResolveResponse.UserSummary summary = new DataScopeResolveResponse.UserSummary();
        summary.setId(user.getId());
        summary.setUserName(user.getUserName());
        summary.setNickName(user.getNickName());
        summary.setDeptId(user.getDeptId());
        summary.setDeptName(resolveDeptName(user.getDeptId()));
        summary.setPosts(resolvePostNames(user.getId()));
        summary.setRoles(resolveRoleSummaries(profile));
        return summary;
    }

    private DataScopeResolveResponse.MenuSummary buildMenuSummary(String scopeKey) {
        if (StringUtils.isBlank(scopeKey)) {
            return null;
        }
        Menu menu = menuService.getOne(Wrappers.lambdaQuery(Menu.class).eq(Menu::getPermission, scopeKey.trim()));
        if (menu == null) {
            DataScopeResolveResponse.MenuSummary summary = new DataScopeResolveResponse.MenuSummary();
            summary.setPermission(scopeKey.trim());
            return summary;
        }
        DataScopeResolveResponse.MenuSummary summary = new DataScopeResolveResponse.MenuSummary();
        summary.setId(menu.getId());
        summary.setName(menu.getName());
        summary.setPermission(menu.getPermission());
        return summary;
    }

    private List<DataScopeResolveResponse.RoleSummary> resolveRoleSummaries(DataScopeProfile profile) {
        List<RoleDataScope> roleScopes = profile.getRoleDataScopes();
        if (roleScopes == null || roleScopes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = roleScopes.stream()
                .map(RoleDataScope::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Role> roleMap = roleIds.isEmpty()
                ? Collections.emptyMap()
                : roleService.listByIds(roleIds).stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(Role::getId, role -> role, (a, b) -> a));
        List<DataScopeResolveResponse.RoleSummary> results = new ArrayList<>();
        for (RoleDataScope scope : roleScopes) {
            if (scope == null) {
                continue;
            }
            DataScopeResolveResponse.RoleSummary summary = new DataScopeResolveResponse.RoleSummary();
            summary.setId(scope.getRoleId());
            summary.setCode(scope.getRoleCode());
            Role role = scope.getRoleId() == null ? null : roleMap.get(scope.getRoleId());
            summary.setName(role != null ? role.getName() : null);
            summary.setDataScopeType(scope.getDataScopeType());
            summary.setDataScopeValue(scope.getCustomDeptIds() == null ? null : joinIds(scope.getCustomDeptIds()));
            results.add(summary);
        }
        return results;
    }

    private List<String> resolvePostNames(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<UserPost> relations = userPostService.list(
                Wrappers.lambdaQuery(UserPost.class).eq(UserPost::getUserId, userId));
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> postIds = relations.stream()
                .map(UserPost::getPostId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postService.listByIds(postIds).stream()
                .filter(Objects::nonNull)
                .map(SysPost::getName)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Dept dept = deptService.getById(deptId);
        return dept == null ? null : dept.getName();
    }

    private UserScopeOverride resolveOverride(DataScopeProfile profile, String scopeKey) {
        Map<String, UserScopeOverride> overrides = profile.getUserScopeOverrides();
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
        if (override.getStatus() != null && override.getStatus() == 0) {
            return null;
        }
        return override;
    }

    private DataScopeResolveResponse.Layer3Summary buildLayer3Summary(UserScopeOverride override) {
        DataScopeResolveResponse.Layer3Summary summary = new DataScopeResolveResponse.Layer3Summary();
        summary.setScopeKey(override.getScopeKey());
        summary.setDataScopeType(override.getDataScopeType());
        summary.setDataScopeValue(joinIds(override.getCustomDeptIds()));
        return summary;
    }

    private DataScopeEvaluator.FinalScope applyOverride(SysUser user, DataScopeProfile profile, UserScopeOverride override) {
        String type = normalizeType(override.getDataScopeType());
        if (DataScopeType.ALL.equals(type)) {
            return DataScopeEvaluator.FinalScope.all();
        }
        if (DataScopeType.SELF.equals(type)) {
            return DataScopeEvaluator.FinalScope.selfOnly(user.getId());
        }
        if (DataScopeType.NONE.equals(type)) {
            return DataScopeEvaluator.FinalScope.none();
        }
        if (DataScopeType.DEPT.equals(type)) {
            return DataScopeEvaluator.FinalScope.ofDept(user.getDeptId());
        }
        if (DataScopeType.DEPT_AND_CHILD.equals(type)) {
            return DataScopeEvaluator.FinalScope.ofDeptIds(profile.getDeptTreeIds());
        }
        if (DataScopeType.CUSTOM_DEPT.equals(type) || DataScopeType.CUSTOM.equals(type)) {
            return DataScopeEvaluator.FinalScope.ofDeptIds(override.getCustomDeptIds());
        }
        return DataScopeEvaluator.FinalScope.selfOnly(user.getId());
    }

    private RoleScopeResult mergeRoleScopes(SysUser user,
                                            DataScopeProfile profile,
                                            List<RoleDataScope> roleScopes,
                                            String scopeKey) {
        boolean hasAny = false;
        boolean includeSelf = false;
        LinkedHashSet<Long> deptIds = new LinkedHashSet<>();
        List<DataScopeResolveResponse.RoleScopeSummary> details = new ArrayList<>();

        for (RoleDataScope roleScope : roleScopes) {
            if (roleScope == null) {
                continue;
            }
            RoleSelection selection = resolveRoleSelection(roleScope, scopeKey);
            if (selection == null) {
                continue;
            }
            hasAny = true;
            DataScopeResolveResponse.RoleScopeSummary summary = new DataScopeResolveResponse.RoleScopeSummary();
            summary.setRoleId(roleScope.getRoleId());
            summary.setRoleCode(roleScope.getRoleCode());
            summary.setRoleName(resolveRoleName(roleScope.getRoleId()));
            summary.setLayer1Type(roleScope.getDataScopeType());
            summary.setLayer2Type(selection.layer2Type);
            summary.setEffectiveType(selection.type);
            summary.setSourceLayer(selection.sourceLayer);
            summary.setCustomDeptIds(selection.customDeptIds);
            details.add(summary);

            if (DataScopeType.ALL.equals(selection.type)) {
                return new RoleScopeResult(DataScopeEvaluator.FinalScope.all(), details, Collections.emptySet(),
                        false, "ALL");
            }
            if (DataScopeType.SELF.equals(selection.type)) {
                includeSelf = true;
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
                if (profile.getDeptTreeIds() != null && !profile.getDeptTreeIds().isEmpty()) {
                    deptIds.addAll(profile.getDeptTreeIds());
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

        if (!deptIds.isEmpty() || includeSelf) {
            String label = buildLabel(deptIds, includeSelf);
            return new RoleScopeResult(DataScopeEvaluator.FinalScope.ofDeptIdsAndSelf(deptIds, includeSelf),
                    details, deptIds, includeSelf, label);
        }
        if (hasAny) {
            return new RoleScopeResult(DataScopeEvaluator.FinalScope.none(), details, Collections.emptySet(),
                    false, "NONE");
        }
        return new RoleScopeResult(DataScopeEvaluator.FinalScope.selfOnly(user.getId()), details,
                Collections.emptySet(), true, "SELF");
    }

    private RoleSelection resolveRoleSelection(RoleDataScope roleScope, String scopeKey) {
        String layer2 = null;
        Set<Long> custom = null;
        if (scopeKey != null && roleScope.getMenuDataScopes() != null) {
            layer2 = normalizeType(roleScope.getMenuDataScopes().get(scopeKey));
            if (layer2 != null && roleScope.getMenuCustomDepts() != null) {
                custom = roleScope.getMenuCustomDepts().get(scopeKey);
            }
        }
        if (layer2 != null) {
            return new RoleSelection(layer2, SOURCE_LAYER_2, roleScope.getDataScopeType(), layer2, custom);
        }
        String layer1 = normalizeType(roleScope.getDataScopeType());
        if (layer1 == null) {
            return null;
        }
        return new RoleSelection(layer1, SOURCE_LAYER_1, roleScope.getDataScopeType(), null, roleScope.getCustomDeptIds());
    }

    private void fillFinal(DataScopeResolveResponse response,
                           DataScopeEvaluator.FinalScope finalScope,
                           String scopeKey) {
        response.setFinalScopeLabel(buildFinalLabel(finalScope));
        response.setMergedDeptIds(finalScope.getDeptIds());
        response.setIncludeSelf(finalScope.isSelf());
        fillRuleAndSql(response, finalScope, scopeKey);
    }

    private void fillRuleAndSql(DataScopeResolveResponse response,
                                DataScopeEvaluator.FinalScope finalScope,
                                String scopeKey) {
        DataScopeRule rule = null;
        Map<String, DataScopeRule> rules = dataScopeRuleProvider == null ? null : dataScopeRuleProvider.getRuleMap();
        if (rules != null && scopeKey != null) {
            rule = rules.get(scopeKey);
        }
        DataScopeResolveResponse.RuleSummary ruleSummary = new DataScopeResolveResponse.RuleSummary();
        if (rule == null) {
            ruleSummary.setSource("DEFAULT");
            ruleSummary.setDeptColumn("create_dept");
            ruleSummary.setUserColumn("create_by");
        } else {
            ruleSummary.setSource("MAPPING");
            ruleSummary.setTableName(rule.getTableName());
            ruleSummary.setTableAlias(rule.getTableAlias());
            ruleSummary.setDeptColumn(rule.getDeptColumn());
            ruleSummary.setUserColumn(rule.getUserColumn());
        }
        response.setRule(ruleSummary);
        response.setSqlCondition(buildSqlCondition(finalScope, ruleSummary, rule));
    }

    private String buildSqlCondition(DataScopeEvaluator.FinalScope finalScope,
                                     DataScopeResolveResponse.RuleSummary ruleSummary,
                                     DataScopeRule rule) {
        if (finalScope == null) {
            return null;
        }
        if (finalScope.isAll()) {
            return "无过滤";
        }
        if (finalScope.isNone()) {
            return "1 = 0";
        }
        String deptColumn = ruleSummary.getDeptColumn();
        String userColumn = ruleSummary.getUserColumn();
        String alias = rule == null ? null : rule.getTableAlias();
        String deptExpr = null;
        if (StringUtils.isNotBlank(deptColumn) && !finalScope.getDeptIds().isEmpty()) {
            deptExpr = withAlias(alias, deptColumn) + " IN (" + joinIds(finalScope.getDeptIds()) + ")";
        }
        String userExpr = null;
        if (finalScope.isSelf() && StringUtils.isNotBlank(userColumn)) {
            userExpr = withAlias(alias, userColumn) + " = :userId";
        }
        if (deptExpr != null && userExpr != null) {
            return "(" + deptExpr + " OR " + userExpr + ")";
        }
        return deptExpr != null ? deptExpr : userExpr;
    }

    private String withAlias(String alias, String column) {
        if (StringUtils.isBlank(alias)) {
            return column;
        }
        return alias.trim() + "." + column;
    }

    private String resolveRoleName(Long roleId) {
        if (roleId == null) {
            return null;
        }
        Role role = roleService.getById(roleId);
        return role == null ? null : role.getName();
    }

    private String buildFinalLabel(DataScopeEvaluator.FinalScope scope) {
        if (scope == null) {
            return "NONE";
        }
        if (scope.isAll()) {
            return "ALL";
        }
        if (scope.isNone()) {
            return "NONE";
        }
        if (scope.isSelf() && scope.getDeptIds().isEmpty()) {
            return "SELF";
        }
        return buildLabel(scope.getDeptIds(), scope.isSelf());
    }

    private String buildLabel(Set<Long> deptIds, boolean includeSelf) {
        String deptLabel = (deptIds == null || deptIds.isEmpty()) ? null : "DEPT";
        if (deptLabel != null && includeSelf) {
            return "DEPT+SELF";
        }
        if (deptLabel != null) {
            return deptLabel;
        }
        if (includeSelf) {
            return "SELF";
        }
        return "NONE";
    }

    private String resolveSourceLayer(DataScopeResolveResponse.Layer3Summary layer3,
                                      List<DataScopeResolveResponse.RoleScopeSummary> roleScopes) {
        if (layer3 != null) {
            return SOURCE_LAYER_3;
        }
        if (roleScopes == null || roleScopes.isEmpty()) {
            return SOURCE_LAYER_1;
        }
        boolean hasLayer2 = roleScopes.stream().anyMatch(item -> SOURCE_LAYER_2.equals(item.getSourceLayer()));
        return hasLayer2 ? SOURCE_LAYER_2 : SOURCE_LAYER_1;
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

    private String joinIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return ids.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Menu> loadUserMenus(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<UserRole> relations = userRoleService.list(
                Wrappers.lambdaQuery(UserRole.class).eq(UserRole::getUserId, userId));
        if (relations == null || relations.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = relations.stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<RoleMenu> roleMenus = roleMenuService.list(
                Wrappers.lambdaQuery(RoleMenu.class).in(RoleMenu::getRoleId, roleIds));
        if (roleMenus == null || roleMenus.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return menuService.listByIds(menuIds);
    }

    private boolean isSuperUser(SysUser user) {
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

    private static final class RoleSelection {
        private final String type;
        private final String sourceLayer;
        @Getter
        private final String layer1Type;
        private final String layer2Type;
        private final Set<Long> customDeptIds;

        private RoleSelection(String type, String sourceLayer, String layer1Type, String layer2Type, Set<Long> customDeptIds) {
            this.type = type;
            this.sourceLayer = sourceLayer;
            this.layer1Type = layer1Type;
            this.layer2Type = layer2Type;
            this.customDeptIds = customDeptIds;
        }

    }

    private static final class RoleScopeResult {
        private final DataScopeEvaluator.FinalScope finalScope;
        private final List<DataScopeResolveResponse.RoleScopeSummary> details;
        private final Set<Long> deptIds;
        private final boolean includeSelf;
        private final String finalLabel;

        private RoleScopeResult(DataScopeEvaluator.FinalScope finalScope,
                                List<DataScopeResolveResponse.RoleScopeSummary> details,
                                Set<Long> deptIds,
                                boolean includeSelf,
                                String finalLabel) {
            this.finalScope = finalScope;
            this.details = details;
            this.deptIds = deptIds;
            this.includeSelf = includeSelf;
            this.finalLabel = finalLabel;
        }
    }
}
