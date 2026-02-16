package com.example.demo.auth.service;

import com.example.demo.auth.dto.UserProfileInfo;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.identity.api.dto.IdentityMenuTreeDTO;
import com.example.demo.identity.api.dto.IdentityRoleDTO;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.example.demo.identity.api.facade.IdentityReadFacade;
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

    private final IdentityReadFacade identityReadFacade;
    private final PermissionProperties permissionProperties;
    private final PasswordPolicyService passwordPolicyService;

    public UserProfileResponse buildProfile(AuthUser authUser) {
        UserProfileResponse response = new UserProfileResponse();
        if (authUser == null || authUser.getId() == null) {
            return response;
        }
        IdentityUserDTO user = identityReadFacade.getUserById(authUser.getId());
        response.setUser(toProfileInfo(authUser, user));
        response.setPasswordExpired(passwordPolicyService.isPasswordExpired(user));
        response.setFirstLoginForceChange(passwordPolicyService.isFirstLoginForceChange(user));
        response.setPasswordChangeRequired(response.isPasswordExpired() || response.isFirstLoginForceChange());
        response.setRoles(loadRoles(authUser.getId()));
        response.setRoleTargets(loadRoleTargets(authUser.getId()));
        response.setPermissions(loadPermissions(authUser));
        response.setMenus(buildMenuTree(loadMenus(authUser)));
        return response;
    }

    private UserProfileInfo toProfileInfo(AuthUser authUser, IdentityUserDTO user) {
        UserProfileInfo info = new UserProfileInfo();
        info.setId(authUser.getId());
        if (user != null) {
            info.setUserName(user.getUserName());
            info.setNickName(user.getNickName());
            info.setPhone(user.getPhone());
            info.setEmail(user.getEmail());
            info.setSex(user.getSex());
            info.setDeptId(user.getDeptId());
            info.setDeptName(authUser.getDeptName());
            info.setDataScopeType(user.getDataScopeType());
            info.setDataScopeValue(user.getDataScopeValue());
        } else {
            info.setUserName(authUser.getUserName());
            info.setNickName(authUser.getNickName());
            info.setDeptId(authUser.getDeptId());
            info.setDeptName(authUser.getDeptName());
            info.setDataScopeType(authUser.getDataScopeType());
            info.setDataScopeValue(authUser.getDataScopeValue());
        }
        if (StringUtils.isBlank(info.getDeptName()) && info.getDeptId() != null) {
            info.setDeptName(identityReadFacade.getDeptNameById(info.getDeptId()));
        }
        return info;
    }

    private List<String> loadRoles(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<String> roles = identityReadFacade.listRoleCodesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<IdentityRoleDTO> loadRoleTargets(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<IdentityRoleDTO> roles = identityReadFacade.listEnabledRolesByUserId(userId);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getId() != null)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(IdentityRoleDTO::getId, role -> role, (left, right) -> left, LinkedHashMap::new),
                        map -> new ArrayList<>(map.values())
                ));
    }

    private List<String> loadPermissions(AuthUser authUser) {
        if (authUser == null || authUser.getId() == null) {
            return Collections.emptyList();
        }
        List<String> permissions = isSuperUser(authUser)
                ? identityReadFacade.listActivePermissionCodes()
                : identityReadFacade.listPermissionCodesByUserId(authUser.getId());
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    private List<IdentityMenuTreeDTO> loadMenus(AuthUser authUser) {
        if (authUser == null || authUser.getId() == null) {
            return Collections.emptyList();
        }
        List<IdentityMenuTreeDTO> menus = isSuperUser(authUser)
                ? identityReadFacade.listActiveMenus()
                : identityReadFacade.listMenusByUserId(authUser.getId());
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        return menus.stream()
                .filter(Objects::nonNull)
                .filter(menu -> menu.getStatus() == null || menu.getStatus() == 1)
                .sorted(Comparator
                        .comparing((IdentityMenuTreeDTO menu) -> menu.getSort() == null ? 0 : menu.getSort())
                        .thenComparing(menu -> menu.getId() == null ? 0L : menu.getId()))
                .collect(Collectors.toList());
    }

    private List<IdentityMenuTreeDTO> buildMenuTree(List<IdentityMenuTreeDTO> menus) {
        if (menus == null || menus.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, IdentityMenuTreeDTO> menuMap = new LinkedHashMap<>();
        for (IdentityMenuTreeDTO menu : menus) {
            if (menu == null || menu.getId() == null) {
                continue;
            }
            menuMap.computeIfAbsent(menu.getId(), key -> copyNode(menu));
        }
        List<IdentityMenuTreeDTO> roots = new ArrayList<>();
        for (IdentityMenuTreeDTO node : menuMap.values()) {
            Long parentId = node.getParentId();
            if (parentId != null && menuMap.containsKey(parentId)) {
                menuMap.get(parentId).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }
        return roots;
    }

    private IdentityMenuTreeDTO copyNode(IdentityMenuTreeDTO menu) {
        IdentityMenuTreeDTO node = new IdentityMenuTreeDTO();
        node.setId(menu.getId());
        node.setName(menu.getName());
        node.setCode(menu.getCode());
        node.setParentId(menu.getParentId());
        node.setPath(menu.getPath());
        node.setComponent(menu.getComponent());
        node.setPermission(menu.getPermission());
        node.setStatus(menu.getStatus());
        node.setSort(menu.getSort());
        node.setRemark(menu.getRemark());
        return node;
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
