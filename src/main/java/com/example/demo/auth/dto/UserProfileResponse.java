package com.example.demo.auth.dto;

import com.example.demo.menu.dto.MenuTreeVO;
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

    private List<MenuTreeVO> menus;
}
