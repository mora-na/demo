package com.example.demo.datascope.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.DataScopeType;
import com.example.demo.datascope.entity.RoleMenuDept;
import com.example.demo.datascope.entity.UserDataScope;
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
import com.example.demo.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录时数据范围画像装配服务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Service
@RequiredArgsConstructor
public class DataScopeProfileService {

    private static final String GLOBAL_SCOPE_KEY = "*";

    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final RoleMenuService roleMenuService;
    private final MenuService menuService;
    private final DeptService deptService;
    private final RoleMenuDeptService roleMenuDeptService;
    private final UserDataScopeService userDataScopeService;

    /**
     * 构建用户数据范围画像。
     *
     * @param user 当前用户
     * @return 数据范围画像
     */
    public DataScopeProfile buildProfile(SysUser user) {
        DataScopeProfile profile = new DataScopeProfile();
        if (user == null || user.getId() == null) {
            return profile;
        }
        profile.setDeptTreeIds(resolveDeptTreeIds(user.getDeptId()));
        profile.setRoleDataScopes(loadRoleScopes(user.getId()));
        profile.setUserScopeOverrides(loadUserOverrides(user.getId()));
        return profile;
    }

    private List<RoleDataScope> loadRoleScopes(Long userId) {
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
        List<Role> roles = roleService.listByIds(roleIds);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, RoleDataScope> roleScopes = new LinkedHashMap<>();
        for (Role role : roles) {
            if (role == null) {
                continue;
            }
            RoleDataScope scope = new RoleDataScope();
            scope.setRoleId(role.getId());
            scope.setRoleCode(role.getCode());
            scope.setDataScopeType(normalizeType(role.getDataScopeType()));
            scope.setCustomDeptIds(parseDeptIds(role.getDataScopeValue()));
            roleScopes.put(role.getId(), scope);
        }
        if (roleScopes.isEmpty()) {
            return Collections.emptyList();
        }

        List<RoleMenu> roleMenus = roleMenuService.list(
                Wrappers.lambdaQuery(RoleMenu.class).in(RoleMenu::getRoleId, roleScopes.keySet()));
        if (roleMenus == null) {
            roleMenus = Collections.emptyList();
        }
        Map<Long, String> menuPermissionMap = loadMenuPermissionMap(roleMenus);
        for (RoleMenu roleMenu : roleMenus) {
            RoleDataScope roleScope = roleScopes.get(roleMenu.getRoleId());
            if (roleScope == null) {
                continue;
            }
            String scopeKey = menuPermissionMap.get(roleMenu.getMenuId());
            if (StringUtils.isBlank(scopeKey)) {
                continue;
            }
            String menuScopeType = normalizeType(roleMenu.getDataScopeType());
            if (menuScopeType == null) {
                continue;
            }
            roleScope.getMenuDataScopes().put(scopeKey, menuScopeType);
        }

        List<RoleMenuDept> customDepts = roleMenuDeptService.list(
                Wrappers.lambdaQuery(RoleMenuDept.class).in(RoleMenuDept::getRoleId, roleScopes.keySet()));
        if (customDepts != null && !customDepts.isEmpty()) {
            for (RoleMenuDept relation : customDepts) {
                RoleDataScope roleScope = roleScopes.get(relation.getRoleId());
                if (roleScope == null) {
                    continue;
                }
                String scopeKey = menuPermissionMap.get(relation.getMenuId());
                if (StringUtils.isBlank(scopeKey) || relation.getDeptId() == null) {
                    continue;
                }
                roleScope.getMenuCustomDepts()
                        .computeIfAbsent(scopeKey, key -> new LinkedHashSet<>())
                        .add(relation.getDeptId());
            }
        }
        return new ArrayList<>(roleScopes.values());
    }

    private Map<String, UserScopeOverride> loadUserOverrides(Long userId) {
        List<UserDataScope> overrides = userDataScopeService.list(
                Wrappers.lambdaQuery(UserDataScope.class).eq(UserDataScope::getUserId, userId));
        if (overrides == null || overrides.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, UserScopeOverride> map = new LinkedHashMap<>();
        for (UserDataScope record : overrides) {
            if (record == null || StringUtils.isBlank(record.getScopeKey())) {
                continue;
            }
            UserScopeOverride override = new UserScopeOverride();
            override.setScopeKey(record.getScopeKey());
            override.setDataScopeType(normalizeType(record.getDataScopeType()));
            override.setCustomDeptIds(parseDeptIds(record.getDataScopeValue()));
            override.setStatus(record.getStatus());
            map.put(record.getScopeKey(), override);
        }
        return map;
    }

    private Map<Long, String> loadMenuPermissionMap(List<RoleMenu> roleMenus) {
        if (roleMenus == null || roleMenus.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<Long> menuIds = roleMenus.stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Menu> menus = menuService.listByIds(menuIds);
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> map = new HashMap<>();
        for (Menu menu : menus) {
            if (menu == null || menu.getId() == null) {
                continue;
            }
            if (StringUtils.isBlank(menu.getPermission())) {
                continue;
            }
            map.put(menu.getId(), menu.getPermission().trim());
        }
        return map;
    }

    private Set<Long> resolveDeptTreeIds(Long rootId) {
        if (rootId == null) {
            return Collections.emptySet();
        }
        List<Dept> depts = deptService.list();
        if (depts == null || depts.isEmpty()) {
            return new LinkedHashSet<>(Collections.singletonList(rootId));
        }
        Map<Long, List<Long>> children = new HashMap<>();
        for (Dept dept : depts) {
            if (dept == null || dept.getId() == null || dept.getParentId() == null) {
                continue;
            }
            children.computeIfAbsent(dept.getParentId(), key -> new ArrayList<>()).add(dept.getId());
        }
        LinkedHashSet<Long> results = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootId);
        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current == null || results.contains(current)) {
                continue;
            }
            results.add(current);
            List<Long> next = children.get(current);
            if (next != null) {
                queue.addAll(next);
            }
        }
        return results;
    }

    private Set<Long> parseDeptIds(String value) {
        if (StringUtils.isBlank(value)) {
            return Collections.emptySet();
        }
        String[] tokens = value.split(",");
        Set<Long> ids = new LinkedHashSet<>();
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            String trimmed = token.trim();
            if (!trimmed.chars().allMatch(Character::isDigit)) {
                continue;
            }
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignore) {
            }
        }
        return ids;
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
}
