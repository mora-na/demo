package com.example.demo.auth.dto;

import lombok.Data;

/**
 * 登出请求载体，支持携带访问令牌。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class LogoutRequest {
    private String token;
}
