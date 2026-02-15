package com.example.demo.system.api.profile;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户角色/权限/菜单聚合信息。
 */
@Data
public class AuthProfileDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> roles = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();
    private List<MenuTreeNodeDTO> menus = new ArrayList<>();
}
