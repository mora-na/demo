package com.example.demo.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 敏感操作验证码校验请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
public class OperationConfirmVerifyRequest {

    /**
     * 操作标识。
     */
    @NotBlank
    @Size(max = 64)
    private String actionKey;

    /**
     * 邮箱验证码。
     */
    @NotBlank
    @Size(max = 12)
    private String code;
}
