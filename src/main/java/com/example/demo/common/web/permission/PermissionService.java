package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthUser;

import java.util.Collection;

public interface PermissionService {

    boolean hasPermission(AuthUser user, String permission);

    boolean hasAllPermissions(AuthUser user, Collection<String> permissions);

    boolean hasAnyPermission(AuthUser user, Collection<String> permissions);
}
