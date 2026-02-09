package com.example.demo.permission.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 权限更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class PermissionUpdateRequest {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 128)
    private String name;
}
