package com.example.demo.permission.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.service.PermissionService;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl extends MppServiceImpl<PermissionMapper, Permission> implements PermissionService {
}
