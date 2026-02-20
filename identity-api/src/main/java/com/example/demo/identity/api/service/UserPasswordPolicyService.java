package com.example.demo.identity.api.service;

/**
 * 身份域密码能力抽象，用于解耦认证实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
public interface UserPasswordPolicyService {

    String resolveRawPassword(String rawPassword);

    boolean isStrongPassword(String rawPassword);

    String decodeTransportPassword(String cipherText);

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);

    boolean forceChangeOnFirstLogin();
}
