package com.example.demo.auth.dto;

import lombok.Data;

/**
 * 敏感操作验证码校验响应。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
public class OperationConfirmVerifyResponse {

    /**
     * 校验通过后签发的操作确认票据。
     */
    private String ticket;

    /**
     * 票据过期时间（Unix 秒时间戳）。
     */
    private long expiresAt;
}
