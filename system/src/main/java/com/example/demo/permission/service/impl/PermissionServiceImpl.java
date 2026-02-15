package com.example.demo.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.service.PermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限服务实现，提供权限状态更新等操作。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    /**
     * 更新权限状态。
     *
     * @param id     权限 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            return false;
        }
        Permission permission = new Permission();
        permission.setId(id);
        permission.setStatus(status);
        return updateById(permission);
    }
}
