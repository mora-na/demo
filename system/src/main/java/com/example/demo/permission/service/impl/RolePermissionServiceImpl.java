package com.example.demo.permission.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.service.RolePermissionService;
import org.springframework.stereotype.Service;

/**
 * 角色-权限关联服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class RolePermissionServiceImpl extends ServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
