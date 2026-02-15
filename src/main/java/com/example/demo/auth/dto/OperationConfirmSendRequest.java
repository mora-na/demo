package com.example.demo.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 敏感操作验证码发送请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
public class OperationConfirmSendRequest {

    /**
     * 操作标识（建议使用权限码或业务动作码）。
     */
    @NotBlank
    @Size(max = 64)
    private String actionKey;

    /**
     * 操作说明（用于邮件展示）。
     */
    @Size(max = 64)
    private String actionLabel;
}
