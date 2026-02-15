package com.example.demo.system.api.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.entity.RoleMenu;
import com.example.demo.menu.service.MenuService;
import com.example.demo.menu.service.RoleMenuService;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.service.PermissionService;
import com.example.demo.permission.service.RoleService;
import com.example.demo.permission.service.UserRoleService;
import com.example.demo.system.api.profile.AuthProfileApi;
import com.example.demo.system.api.profile.AuthProfileDTO;
import com.example.demo.system.api.profile.MenuTreeNodeDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthProfileApiImpl implements AuthProfileApi {

    private final PermissionMapper permissionMapper;
    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final RoleMenuService roleMenuService;
    private final MenuService menuService;

    @Override
    public AuthProfileDTO buildProfile(Long userId, boolean superUser) {
        AuthProfileDTO profile = new AuthProfileDTO();
        if (userId == null) {
            return profile;
        }
        List<Role> roles = loadRoles(userId);
        profile.setRoles(toRoleCodes(roles));
        profile.setPermissions(loadPermissions(userId, superUser));
        profile.setMenus(buildMenuTree(loadMenus(roles, superUser)));
        return profile;
    }

    private List<Role> loadRoles(Long userId) {
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
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(role -> role != null && (role.getStatus() == null || role.getStatus() == 1))
                .collect(Collectors.toList());
    }

    private List<String> toRoleCodes(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(Role::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> loadPermissions(Long userId, boolean superUser) {
        List<String> permissions;
        if (superUser) {
            permissions = permissionService.list().stream()
                    .filter(permission -> permission != null && permission.getStatus() != null && permission.getStatus() == 1)
                    .map(Permission::getCode)
                    .collect(Collectors.toList());
        } else {
            permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        }
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<Menu> loadMenus(List<Role> roles, boolean superUser) {
        List<Menu> menus;
        if (superUser) {
            menus = menuService.list(Wrappers.lambdaQuery(Menu.class).eq(Menu::getStatus, 1));
        } else {
            List<Long> roleIds = roles == null ? Collections.emptyList()
                    : roles.stream().map(Role::getId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
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
            menus = menuService.listByIds(menuIds);
        }
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        return menus.stream()
                .filter(menu -> menu != null && (menu.getStatus() == null || menu.getStatus() == 1))
                .sorted(Comparator
                        .comparing((Menu menu) -> menu.getSort() == null ? 0 : menu.getSort())
                        .thenComparing(menu -> menu.getId() == null ? 0L : menu.getId()))
                .collect(Collectors.toList());
    }

    private List<MenuTreeNodeDTO> buildMenuTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, MenuTreeNodeDTO> menuMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            if (menu == null || menu.getId() == null) {
                continue;
            }
            menuMap.computeIfAbsent(menu.getId(), key -> toTree(menu));
        }
        List<MenuTreeNodeDTO> roots = new ArrayList<>();
        for (MenuTreeNodeDTO node : menuMap.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && menuMap.containsKey(parentId)) {
                menuMap.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private MenuTreeNodeDTO toTree(Menu menu) {
        MenuTreeNodeDTO view = new MenuTreeNodeDTO();
        view.setId(menu.getId());
        view.setName(menu.getName());
        view.setCode(menu.getCode());
        view.setParentId(menu.getParentId());
        view.setPath(menu.getPath());
        view.setComponent(menu.getComponent());
        view.setPermission(menu.getPermission());
        view.setStatus(menu.getStatus());
        view.setSort(menu.getSort());
        view.setRemark(menu.getRemark());
        return view;
    }
}
