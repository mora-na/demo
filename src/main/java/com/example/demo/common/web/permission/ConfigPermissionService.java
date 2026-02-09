package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于配置的权限服务实现，读取配置中的用户权限映射。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnProperty(prefix = "security.permission", name = "source", havingValue = "config")
public class ConfigPermissionService implements PermissionService {

    private final PermissionProperties properties;

    /**
     * 构造函数，注入权限配置。
     *
     * @param properties 权限配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public ConfigPermissionService(PermissionProperties properties) {
        this.properties = properties;
    }

    /**
     * 判断用户是否具备指定权限。
     *
     * @param user       用户信息
     * @param permission 权限码
     * @return true 表示具备
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean hasPermission(AuthUser user, String permission) {
        if (user == null || permission == null || permission.trim().isEmpty()) {
            return false;
        }
        if (isSuperUser(user)) {
            return true;
        }
        return getPermissions(user).contains(permission);
    }

    /**
     * 判断用户是否具备所有权限。
     *
     * @param user        用户信息
     * @param permissions 权限列表
     * @return true 表示全部具备
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean hasAllPermissions(AuthUser user, Collection<String> permissions) {
        if (user == null) {
            return false;
        }
        if (permissions == null || permissions.isEmpty()) {
            return true;
        }
        if (isSuperUser(user)) {
            return true;
        }
        Set<String> current = getPermissions(user);
        for (String permission : permissions) {
            if (!current.contains(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断用户是否具备任一权限。
     *
     * @param user        用户信息
     * @param permissions 权限列表
     * @return true 表示至少具备一个
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean hasAnyPermission(AuthUser user, Collection<String> permissions) {
        if (user == null) {
            return false;
        }
        if (permissions == null || permissions.isEmpty()) {
            return true;
        }
        if (isSuperUser(user)) {
            return true;
        }
        Set<String> current = getPermissions(user);
        for (String permission : permissions) {
            if (current.contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为超级用户。
     *
     * @param user 用户信息
     * @return true 表示超级用户
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isSuperUser(AuthUser user) {
        if (user == null || properties == null) {
            return false;
        }
        List<String> superUsers = properties.getSuperUsers();
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

    /**
     * 获取用户权限集合。
     *
     * @param user 用户信息
     * @return 权限集合
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Set<String> getPermissions(AuthUser user) {
        if (user == null || properties == null) {
            return Collections.emptySet();
        }
        Map<String, List<String>> userPermissions = properties.getUserPermissions();
        if (userPermissions == null || userPermissions.isEmpty()) {
            return Collections.emptySet();
        }
        String userName = user.getUserName();
        if (userName == null) {
            return Collections.emptySet();
        }
        List<String> permissions = userPermissions.get(userName);
        if (permissions == null || permissions.isEmpty()) {
            permissions = userPermissions.get(userName.toLowerCase(Locale.ROOT));
        }
        if (permissions == null) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .collect(Collectors.toSet());
    }
}
