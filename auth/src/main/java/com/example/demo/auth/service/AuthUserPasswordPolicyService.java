package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.system.api.user.UserPasswordPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户模块密码策略实现，桥接到认证模块能力。
 */
@Service
@RequiredArgsConstructor
public class AuthUserPasswordPolicyService implements UserPasswordPolicyService {

    private final PasswordService passwordService;
    private final AuthProperties authProperties;

    @Override
    public String resolveRawPassword(String rawPassword) {
        return passwordService.resolveRawPassword(rawPassword);
    }

    @Override
    public boolean isStrongPassword(String rawPassword) {
        return passwordService.isStrongPassword(rawPassword);
    }

    @Override
    public String decodeTransportPassword(String cipherText) {
        return passwordService.decodeTransportPassword(cipherText);
    }

    @Override
    public String encode(String rawPassword) {
        return passwordService.encode(rawPassword);
    }

    @Override
    public boolean forceChangeOnFirstLogin() {
        return authProperties.getPassword().isForceChangeOnFirstLogin();
    }
}
