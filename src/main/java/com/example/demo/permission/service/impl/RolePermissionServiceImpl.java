package com.example.demo.permission.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.service.RolePermissionService;
import org.springframework.stereotype.Service;

@Service
public class RolePermissionServiceImpl extends MppServiceImpl<RolePermissionMapper, RolePermission> implements RolePermissionService {
}
