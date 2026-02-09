package com.example.demo.permission.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.permission.entity.UserRole;
import com.example.demo.permission.mapper.UserRoleMapper;
import com.example.demo.permission.service.UserRoleService;
import org.springframework.stereotype.Service;

/**
 * 用户-角色关联服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class UserRoleServiceImpl extends MppServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
}
