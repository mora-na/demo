package com.example.demo.permission.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.mapper.UserRoleMapper;
import com.example.demo.permission.service.UserRoleService;
import org.springframework.stereotype.Service;

@Service
public class UserRoleServiceImpl extends MppServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
