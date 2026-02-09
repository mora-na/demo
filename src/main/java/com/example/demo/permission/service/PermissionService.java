package com.example.demo.permission.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.permission.entity.Permission;

public interface PermissionService extends IMppService<Permission> {

    boolean updateStatus(Long id, Integer status);
}
