package com.example.demo.permission.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.permission.entity.Role;

import java.util.List;

/**
 * 角色服务接口，封装角色与权限关系的业务能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface RoleService extends IMppService<Role> {

    /**
     * 为角色分配权限集合。
     *
     * @param roleId        角色 ID
     * @param permissionIds 权限 ID 集合
     * @return true 表示分配成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 更新角色状态。
     *
     * @param roleId 角色 ID
     * @param status 状态值
     * @return true 表示更新成功
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean updateStatus(Long roleId, Integer status);
}
