package com.example.demo.auth.dto;

import com.example.demo.auth.model.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应载体，包含访问令牌、类型、过期时间与用户摘要。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresAt;
    private AuthUser user;
}
