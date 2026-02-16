package com.example.demo.identity.api.facade;

import com.example.demo.identity.api.dto.IdentityRoleDTO;

import java.util.Collection;
import java.util.List;

/**
 * 身份角色接口，提供基于用户/角色/权限的查询能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public interface IdentityRoleApi {

    List<Long> listAllEnabledUserIds();

    List<Long> listEnabledUserIdsByIds(Collection<Long> userIds);

    List<Long> listEnabledUserIdsByDeptIds(Collection<Long> deptIds);

    List<Long> listEnabledUserIdsByRoleIds(Collection<Long> roleIds);

    List<IdentityRoleDTO> listEnabledRolesByUserId(Long userId);

    List<String> listRoleCodesByUserId(Long userId);

    List<String> listPermissionCodesByUserId(Long userId);

    List<String> listActivePermissionCodes();
}
