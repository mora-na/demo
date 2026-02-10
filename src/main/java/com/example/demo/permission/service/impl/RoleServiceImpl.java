package com.example.demo.permission.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

/**
 * 角色服务实现，负责角色权限关系维护与状态更新。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RolePermissionService rolePermissionService;

    /**
     * 为角色重置并分配权限集合。
     *
     * @param roleId        角色 ID
     * @param permissionIds 权限 ID 集合
     * @return true 表示分配成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 更新角色状态。
     *
     * @param roleId 角色 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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
