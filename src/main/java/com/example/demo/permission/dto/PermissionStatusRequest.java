package com.example.demo.permission.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 权限状态更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class PermissionStatusRequest {

    @NotNull
    private Integer status;
}
