package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 密码处理服务，支持 plain、md5、bcrypt 模式。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class PasswordService {

    private final AuthProperties authProperties;
    private final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

    /**
     * 校验原始密码与已编码密码是否匹配。
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 已编码密码
     * @return 匹配返回 true
     */
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        String mode = normalizeMode(authProperties.getPassword().getMode());
        if ("bcrypt".equals(mode)) {
            return bcryptPasswordEncoder.matches(rawPassword, encodedPassword);
        }
        if ("md5".equals(mode)) {
            return md5(rawPassword).equalsIgnoreCase(encodedPassword);
        }
        return rawPassword.equals(encodedPassword);
    }

    /**
     * 对原始密码进行编码。
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码
     */
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        String mode = normalizeMode(authProperties.getPassword().getMode());
        if ("bcrypt".equals(mode)) {
            return bcryptPasswordEncoder.encode(rawPassword);
        }
        if ("md5".equals(mode)) {
            return md5(rawPassword);
        }
        return rawPassword;
    }

    /**
     * 计算带盐的 MD5 值。
     *
     * @param rawPassword 原始密码
     * @return MD5 字符串
     */
    private String md5(String rawPassword) {
        String salt = authProperties.getPassword().getSalt();
        String value = (salt == null ? "" : salt) + rawPassword;
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 规范化密码模式配置。
     *
     * @param mode 配置的模式字符串
     * @return 规范化后的模式
     */
    private String normalizeMode(String mode) {
        return mode == null ? "plain" : mode.trim().toLowerCase();
    }
}
