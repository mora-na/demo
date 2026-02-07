package com.example.demo.auth.dto;

import com.example.demo.auth.model.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresAt;
    private AuthUser user;
}
