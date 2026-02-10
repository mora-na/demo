package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;
import com.example.demo.permission.mapper.PermissionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于数据库的权限服务实现，支持 Redis 缓存加速。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnProperty(prefix = "security.permission", name = "source", havingValue = "db", matchIfMissing = true)
public class DbPermissionService implements PermissionService {

    private static final String PERMISSION_KEY_PREFIX = "perm:user:";
    private final PermissionMapper permissionMapper;
    private final PermissionProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数，注入权限 Mapper 与缓存配置。
     *
     * @param permissionMapper 权限 Mapper
     * @param properties       权限配置
     * @param redisTemplate    Redis 模板
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DbPermissionService(PermissionMapper permissionMapper,
                               PermissionProperties properties,
                               RedisTemplate<String, Object> redisTemplate) {
        this.permissionMapper = permissionMapper;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
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
     * 获取用户权限集合，优先从缓存读取。
     *
     * @param user 用户信息
     * @return 权限集合
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Set<String> getPermissions(AuthUser user) {
        if (user == null || user.getId() == null) {
            return Collections.emptySet();
        }
        long ttlSeconds = properties.getCacheSeconds();
        if (ttlSeconds <= 0) {
            return queryPermissions(user.getId());
        }
        String key = buildKey(user.getId());
        Set<String> cached = readCachedPermissions(key);
        if (cached != null) {
            return cached;
        }
        Set<String> fresh = queryPermissions(user.getId());
        redisTemplate.opsForValue().set(key, fresh, Duration.ofSeconds(ttlSeconds));
        return fresh;
    }

    /**
     * 从数据库查询用户权限。
     *
     * @param userId 用户 ID
     * @return 权限集合
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Set<String> queryPermissions(Long userId) {
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .collect(Collectors.toSet());
    }

    /**
     * 读取缓存中的权限集合。
     *
     * @param key Redis Key
     * @return 权限集合，未命中返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @SuppressWarnings("unchecked")
    private Set<String> readCachedPermissions(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Set) {
            return (Set<String>) value;
        }
        if (value instanceof Collection) {
            return new HashSet<>((Collection<String>) value);
        }
        return null;
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
        if (user == null) {
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
     * 构建权限缓存 Key。
     *
     * @param userId 用户 ID
     * @return 缓存 Key
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String buildKey(Long userId) {
        return PERMISSION_KEY_PREFIX + userId;
    }
}
