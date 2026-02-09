package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;
import com.example.demo.permission.mapper.PermissionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "security.permission", name = "source", havingValue = "db", matchIfMissing = true)
public class DbPermissionService implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PERMISSION_KEY_PREFIX = "perm:user:";

    public DbPermissionService(PermissionMapper permissionMapper,
                               PermissionProperties properties,
                               RedisTemplate<String, Object> redisTemplate) {
        this.permissionMapper = permissionMapper;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

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

    private Set<String> queryPermissions(Long userId) {
        List<String> permissions = permissionMapper.selectPermissionCodesByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptySet();
        }
        return permissions.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .collect(Collectors.toSet());
    }

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

    private String buildKey(Long userId) {
        return PERMISSION_KEY_PREFIX + userId;
    }
}
