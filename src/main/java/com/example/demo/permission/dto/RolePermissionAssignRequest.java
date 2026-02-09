package com.example.demo.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色权限分配请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class RolePermissionAssignRequest {

    private List<Long> permissionIds;
}
