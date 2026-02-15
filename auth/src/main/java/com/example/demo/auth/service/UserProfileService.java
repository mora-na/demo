package com.example.demo.auth.service;

import com.example.demo.auth.dto.UserProfileInfo;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.system.api.profile.AuthProfileApi;
import com.example.demo.system.api.profile.AuthProfileDTO;
import com.example.demo.system.api.user.UserAccountApi;
import com.example.demo.system.api.user.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 登录用户画像服务，汇总用户信息、角色、权限与菜单。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final PermissionProperties permissionProperties;
    private final UserAccountApi userAccountApi;
    private final AuthProfileApi authProfileApi;
    private final PasswordPolicyService passwordPolicyService;

    public UserProfileResponse buildProfile(AuthUser authUser) {
        UserProfileResponse response = new UserProfileResponse();
        if (authUser == null || authUser.getId() == null) {
            return response;
        }
        UserAccountDTO user = userAccountApi.getById(authUser.getId());
        response.setUser(toProfileInfo(authUser, user));
        response.setPasswordExpired(passwordPolicyService.isPasswordExpired(user));
        response.setFirstLoginForceChange(passwordPolicyService.isFirstLoginForceChange(user));
        response.setPasswordChangeRequired(response.isPasswordExpired() || response.isFirstLoginForceChange());
        AuthProfileDTO profile = authProfileApi.buildProfile(authUser.getId(), isSuperUser(authUser));
        if (profile == null) {
            response.setRoles(Collections.emptyList());
            response.setPermissions(Collections.emptyList());
            response.setMenus(Collections.emptyList());
        } else {
            response.setRoles(profile.getRoles() == null ? Collections.emptyList() : profile.getRoles());
            response.setPermissions(profile.getPermissions() == null ? Collections.emptyList() : profile.getPermissions());
            response.setMenus(profile.getMenus() == null ? Collections.emptyList() : profile.getMenus());
        }
        return response;
    }

    private UserProfileInfo toProfileInfo(AuthUser authUser, UserAccountDTO user) {
        UserProfileInfo info = new UserProfileInfo();
        info.setId(authUser.getId());
        if (user != null) {
            info.setUserName(user.getUserName());
            info.setNickName(user.getNickName());
            info.setPhone(user.getPhone());
            info.setEmail(user.getEmail());
            info.setSex(user.getSex());
            info.setDeptId(user.getDeptId());
            info.setDataScopeType(user.getDataScopeType());
            info.setDataScopeValue(user.getDataScopeValue());
        } else {
            info.setUserName(authUser.getUserName());
            info.setNickName(authUser.getNickName());
            info.setDeptId(authUser.getDeptId());
            info.setDataScopeType(authUser.getDataScopeType());
            info.setDataScopeValue(authUser.getDataScopeValue());
        }
        return info;
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
