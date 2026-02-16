package com.example.demo.identity.facade;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.datascope.model.DataScopeProfile;
import com.example.demo.datascope.service.DataScopeProfileService;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import com.example.demo.identity.api.dto.IdentityDataScopeProfileDTO;
import com.example.demo.identity.api.dto.IdentityMenuTreeDTO;
import com.example.demo.identity.api.dto.IdentityRoleDTO;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.example.demo.identity.api.facade.IdentityReadFacade;
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
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 身份域读接口实现，封装身份相关查询，避免外部模块直接依赖身份域内部服务和实体。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class IdentityReadFacadeImpl implements IdentityReadFacade {

    private final SysUserService userService;
    private final UserRoleService userRoleService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;
    private final RoleMenuService roleMenuService;
    private final MenuService menuService;
    private final DataScopeProfileService dataScopeProfileService;
    private final DeptService deptService;

    @Override
    public IdentityUserDTO getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return toDto(userService.getById(userId));
    }

    @Override
    public IdentityUserDTO getUserByUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return null;
        }
        return toDto(userService.getByUserName(userName.trim()));
    }

    @Override
    public List<IdentityUserDTO> listUsersByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = normalizeIds(userIds);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return userService.listByIds(ids).stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> listAllEnabledUserIds() {
        List<SysUser> users = userService.list(Wrappers.lambdaQuery(SysUser.class)
                .eq(SysUser::getStatus, SysUser.STATUS_ENABLED));
        return toEnabledUserIds(users);
    }

    @Override
    public List<Long> listEnabledUserIdsByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = normalizeIds(userIds);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysUser> users = userService.listByIds(ids);
        return toEnabledUserIds(users);
    }

    @Override
    public List<Long> listEnabledUserIdsByDeptIds(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = normalizeIds(deptIds);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysUser> users = userService.list(Wrappers.lambdaQuery(SysUser.class)
                .in(SysUser::getDeptId, ids)
                .eq(SysUser::getStatus, SysUser.STATUS_ENABLED));
        return toEnabledUserIds(users);
    }

    @Override
    public List<Long> listEnabledUserIdsByRoleIds(Collection<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = normalizeIds(roleIds);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIds = userRoleService.list(Wrappers.lambdaQuery(UserRole.class)
                        .in(UserRole::getRoleId, ids))
                .stream()
                .map(UserRole::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return listEnabledUserIdsByIds(userIds);
    }

    @Override
    public List<IdentityRoleDTO> listEnabledRolesByUserId(Long userId) {
        List<Role> roles = listEnabledRoles(userId);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(role -> {
                    IdentityRoleDTO dto = new IdentityRoleDTO();
                    dto.setId(role.getId());
                    dto.setCode(role.getCode());
                    dto.setName(role.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listRoleCodesByUserId(Long userId) {
        List<Role> roles = listEnabledRoles(userId);
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(Role::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listPermissionCodesByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> listActivePermissionCodes() {
        List<Permission> permissions = permissionService.list(Wrappers.lambdaQuery(Permission.class)
                .eq(Permission::getStatus, 1));
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(Objects::nonNull)
                .map(Permission::getCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public List<IdentityMenuTreeDTO> listMenusByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoleService.list(Wrappers.lambdaQuery(UserRole.class)
                        .eq(UserRole::getUserId, userId))
                .stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> menuIds = roleMenuService.list(Wrappers.lambdaQuery(RoleMenu.class)
                        .in(RoleMenu::getRoleId, roleIds))
                .stream()
                .map(RoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (menuIds.isEmpty()) {
            return Collections.emptyList();
        }
        return menuService.listByIds(menuIds).stream()
                .filter(Objects::nonNull)
                .filter(menu -> menu.getStatus() == null || menu.getStatus() == 1)
                .map(this::toMenuDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<IdentityMenuTreeDTO> listActiveMenus() {
        return menuService.list(Wrappers.lambdaQuery(Menu.class).eq(Menu::getStatus, 1)).stream()
                .filter(Objects::nonNull)
                .map(this::toMenuDto)
                .collect(Collectors.toList());
    }

    @Override
    public IdentityDataScopeProfileDTO buildDataScopeProfile(Long userId) {
        IdentityDataScopeProfileDTO dto = new IdentityDataScopeProfileDTO();
        if (userId == null) {
            return dto;
        }
        SysUser user = userService.getById(userId);
        if (user == null) {
            return dto;
        }
        DataScopeProfile profile = dataScopeProfileService.buildProfile(user);
        if (profile == null) {
            return dto;
        }
        dto.setDeptTreeIds(profile.getDeptTreeIds());
        dto.setRoleDataScopes(profile.getRoleDataScopes());
        dto.setUserScopeOverrides(profile.getUserScopeOverrides());
        return dto;
    }

    @Override
    public String getDeptNameById(Long deptId) {
        if (deptId == null) {
            return null;
        }
        Dept dept = deptService.getById(deptId);
        return dept == null ? null : dept.getName();
    }

    private List<Long> normalizeIds(Collection<Long> ids) {
        return ids.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Role> listEnabledRoles(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoleService.list(Wrappers.lambdaQuery(UserRole.class)
                        .eq(UserRole::getUserId, userId))
                .stream()
                .map(UserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleService.listByIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getStatus() == null || role.getStatus() == 1)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(Role::getId, role -> role, (left, right) -> left, LinkedHashMap::new),
                        map -> new java.util.ArrayList<>(map.values())
                ));
    }

    private List<Long> toEnabledUserIds(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getStatus() == null || user.getStatus().equals(SysUser.STATUS_ENABLED))
                .map(SysUser::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private IdentityUserDTO toDto(SysUser user) {
        if (user == null) {
            return null;
        }
        IdentityUserDTO dto = new IdentityUserDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setNickName(user.getNickName());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setSex(user.getSex());
        dto.setStatus(user.getStatus());
        dto.setDeptId(user.getDeptId());
        dto.setDataScopeType(user.getDataScopeType());
        dto.setDataScopeValue(user.getDataScopeValue());
        dto.setPassword(user.getPassword());
        dto.setPasswordUpdatedAt(user.getPasswordUpdatedAt());
        dto.setForcePasswordChange(user.getForcePasswordChange());
        return dto;
    }

    private IdentityMenuTreeDTO toMenuDto(Menu menu) {
        IdentityMenuTreeDTO dto = new IdentityMenuTreeDTO();
        dto.setId(menu.getId());
        dto.setName(menu.getName());
        dto.setCode(menu.getCode());
        dto.setParentId(menu.getParentId());
        dto.setPath(menu.getPath());
        dto.setComponent(menu.getComponent());
        dto.setPermission(menu.getPermission());
        dto.setStatus(menu.getStatus());
        dto.setSort(menu.getSort());
        dto.setRemark(menu.getRemark());
        return dto;
    }
}
