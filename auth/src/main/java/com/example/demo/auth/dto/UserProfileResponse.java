package com.example.demo.auth.dto;

import com.example.demo.identity.api.dto.IdentityMenuTreeDTO;
import com.example.demo.identity.api.dto.IdentityRoleDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户画像响应，包含用户信息、角色、权限与菜单。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class UserProfileResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UserProfileInfo user;

    private List<String> roles;

    private List<String> permissions;

    private List<IdentityMenuTreeDTO> menus;

    private List<IdentityRoleDTO> roleTargets;

    /**
     * 是否需要强制修改密码。
     */
    private boolean passwordChangeRequired;

    /**
     * 是否命中密码过期策略。
     */
    private boolean passwordExpired;

    /**
     * 是否命中首次登录强制改密策略。
     */
    private boolean firstLoginForceChange;
}
