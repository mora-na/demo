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
    /**
     * 是否需要强制修改密码。
     */
    private boolean passwordChangeRequired;
    /**
     * 是否因密码已过期而触发强制修改。
     */
    private boolean passwordExpired;
    /**
     * 是否因首次登录策略触发强制修改。
     */
    private boolean firstLoginForceChange;
    /**
     * 密码过期天数配置（<=0 表示不启用）。
     */
    private Integer passwordExpireDays;
}
