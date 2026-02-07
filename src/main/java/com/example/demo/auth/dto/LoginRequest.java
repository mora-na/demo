package com.example.demo.auth.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;
    private String captchaId;
    private String captchaCode;
}
