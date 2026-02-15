package com.example.demo.user.service;

/**
 * 用户模块的密码能力抽象，用于解耦具体认证实现。
 */
public interface UserPasswordPolicyService {

    /**
     * 解析用户输入密码（含默认密码回退与传输层解码）。
     */
    String resolveRawPassword(String rawPassword);

    /**
     * 判断密码强度是否满足策略。
     */
    boolean isStrongPassword(String rawPassword);

    /**
     * 仅执行传输层密码解码，不做默认密码回退。
     */
    String decodeTransportPassword(String cipherText);

    /**
     * 编码密码。
     */
    String encode(String rawPassword);

    /**
     * 是否在首次登录时强制修改密码。
     */
    boolean forceChangeOnFirstLogin();
}
