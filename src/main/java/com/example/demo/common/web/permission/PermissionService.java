package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;

import java.util.Collection;

/**
 * 权限服务接口，提供权限校验的基础能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface PermissionService {

    /**
     * 判断用户是否具备指定权限。
     *
     * @param user       用户信息
     * @param permission 权限码
     * @return true 表示具备权限
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean hasPermission(AuthUser user, String permission);

    /**
     * 判断用户是否具备所有权限。
     *
     * @param user        用户信息
     * @param permissions 权限列表
     * @return true 表示全部具备
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean hasAllPermissions(AuthUser user, Collection<String> permissions);

    /**
     * 判断用户是否具备任一权限。
     *
     * @param user        用户信息
     * @param permissions 权限列表
     * @return true 表示至少具备一个
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean hasAnyPermission(AuthUser user, Collection<String> permissions);
}
