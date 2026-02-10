package com.example.demo.menu.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色菜单分配请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class RoleMenuAssignRequest {

    private List<Long> menuIds;
}
