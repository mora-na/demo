package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户角色分配请求体，携带角色 ID 列表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class SysUserRoleAssignRequest {

    @NotNull
    private List<Long> roleIds;
}
