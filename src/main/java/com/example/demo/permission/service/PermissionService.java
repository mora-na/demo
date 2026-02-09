package com.example.demo.permission.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.permission.entity.Permission;

/**
 * 权限服务接口，封装权限业务能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface PermissionService extends IMppService<Permission> {

    /**
     * 更新权限状态。
     *
     * @param id     权限 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean updateStatus(Long id, Integer status);
}
