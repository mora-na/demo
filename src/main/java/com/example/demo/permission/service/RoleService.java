package com.example.demo.permission.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.permission.entity.Role;

import java.util.List;

public interface RoleService extends IMppService<Role> {

    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    boolean updateStatus(Long roleId, Integer status);
}
