package com.example.demo.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.auth.dto.UserProfileInfo;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.menu.dto.MenuTreeVO;
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
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录用户画像服务，汇总用户信息、角色、权限与菜单。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final PermissionMapper permissionMapper;
    private final PermissionService permissionService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final RoleMenuService roleMenuService;
    private final MenuService menuService;
    private final PermissionProperties permissionProperties;

    public UserProfileResponse buildProfile(AuthUser authUser) {
        UserProfileResponse response = new UserProfileResponse();
        if (authUser == null || authUser.getId() == null) {
            return response;
        }
        response.setUser(toProfileInfo(authUser));
        List<Role> roles = loadRoles(authUser.getId());
        response.setRoles(toRoleCodes(roles));
        response.setPermissions(loadPermissions(authUser));
        response.setMenus(buildMenuTree(loadMenus(authUser, roles)));
        return response;
    }

    private UserProfileInfo toProfileInfo(AuthUser authUser) {
        UserProfileInfo info = new UserProfileInfo();
        info.setId(authUser.getId());
        info.setUserName(authUser.getUserName());
        info.setNickName(authUser.getNickName());
        info.setDeptId(authUser.getDeptId());
        info.setDataScopeType(authUser.getDataScopeType());
        info.setDataScopeValue(authUser.getDataScopeValue());
        return info;
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

    private List<String> loadPermissions(AuthUser authUser) {
        if (authUser == null || authUser.getId() == null) {
            return Collections.emptyList();
        }
        List<String> permissions;
        if (isSuperUser(authUser)) {
            permissions = permissionService.list().stream()
                    .filter(permission -> permission != null && permission.getStatus() != null && permission.getStatus() == 1)
                    .map(Permission::getCode)
                    .collect(Collectors.toList());
        } else {
            permissions = permissionMapper.selectPermissionCodesByUserId(authUser.getId());
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

    private List<Menu> loadMenus(AuthUser authUser, List<Role> roles) {
        if (authUser == null || authUser.getId() == null) {
            return Collections.emptyList();
        }
        List<Menu> menus;
        if (isSuperUser(authUser)) {
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

    private List<MenuTreeVO> buildMenuTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, MenuTreeVO> menuMap = new LinkedHashMap<>();
        for (Menu menu : menus) {
            if (menu == null || menu.getId() == null) {
                continue;
            }
            if (!menuMap.containsKey(menu.getId())) {
                menuMap.put(menu.getId(), toTreeVO(menu));
            }
        }
        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO node : menuMap.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && menuMap.containsKey(parentId)) {
                menuMap.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private MenuTreeVO toTreeVO(Menu menu) {
        MenuTreeVO view = new MenuTreeVO();
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

    private boolean isSuperUser(AuthUser user) {
        if (user == null) {
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
}
