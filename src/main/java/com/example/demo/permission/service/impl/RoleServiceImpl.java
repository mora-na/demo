package com.example.demo.permission.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends MppServiceImpl<RoleMapper, Role> implements RoleService {
}
