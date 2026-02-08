package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(prefix = "security.permission", name = "source", havingValue = "config")
public class ConfigPermissionService implements PermissionService {

    private final PermissionProperties properties;

    public ConfigPermissionService(PermissionProperties properties) {
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
