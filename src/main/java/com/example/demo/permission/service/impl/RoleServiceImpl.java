package com.example.demo.permission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.service.RolePermissionService;
import com.example.demo.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends MppServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionService rolePermissionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(Long roleId, List<Long> permissionIds) {
        if (roleId == null) {
            return false;
        }
        rolePermissionService.remove(Wrappers.lambdaQuery(RolePermission.class).eq(RolePermission::getRoleId, roleId));
        if (permissionIds == null || permissionIds.isEmpty()) {
            return true;
        }
        List<RolePermission> relations = permissionIds.stream()
                .filter(id -> id != null)
                .distinct()
                .map(pid -> new RolePermission(null, roleId, pid))
                .collect(Collectors.toList());
        if (relations.isEmpty()) {
            return true;
        }
        return rolePermissionService.saveBatch(relations);
    }

    @Override
    public boolean updateStatus(Long roleId, Integer status) {
        if (roleId == null) {
            return false;
        }
        Role role = new Role();
        role.setId(roleId);
        role.setStatus(status);
        return updateById(role);
    }
}
