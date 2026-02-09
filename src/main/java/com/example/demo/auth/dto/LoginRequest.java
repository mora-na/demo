package com.example.demo.auth.dto;

import lombok.Data;

/**
 * 登录请求参数载体，包含账号、密码与验证码信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class LoginRequest {
    private String userName;
    private String password;
    private String captchaId;
    private String captchaCode;
}
