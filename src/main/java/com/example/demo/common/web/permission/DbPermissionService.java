package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;
import com.example.demo.permission.mapper.PermissionMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "security.permission", name = "source", havingValue = "db", matchIfMissing = true)
public class DbPermissionService implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final PermissionProperties properties;
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public DbPermissionService(PermissionMapper permissionMapper, PermissionProperties properties) {
        this.permissionMapper = permissionMapper;
        this.properties = properties;
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
        long now = System.currentTimeMillis();
        CacheEntry entry = cache.get(user.getId());
        if (entry != null && entry.expiresAt > now) {
            return entry.permissions;
        }
        Set<String> fresh = queryPermissions(user.getId());
        cache.put(user.getId(), new CacheEntry(fresh, now + ttlSeconds * 1000L));
        cleanupIfNeeded(now);
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

    private void cleanupIfNeeded(long now) {
        int maxSize = Math.max(1000, properties.getMaxCacheSize());
        if (cache.size() <= maxSize) {
            return;
        }
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
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

    private static final class CacheEntry {
        private final Set<String> permissions;
        private final long expiresAt;

        private CacheEntry(Set<String> permissions, long expiresAt) {
            this.permissions = permissions;
            this.expiresAt = expiresAt;
        }
    }
}
