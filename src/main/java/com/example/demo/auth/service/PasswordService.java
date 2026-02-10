package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

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
     * 解密传输中的密码（支持 plain/aes-gcm/base64）。
     *
     * @param cipherText 传输中的密码
     * @return 解密后的明文密码，解密失败返回 null
     */
    public String decodeTransportPassword(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        String mode = normalizeTransportMode(authProperties.getPassword().getTransportMode());
        if ("plain".equals(mode)) {
            return cipherText;
        }
        if ("base64".equals(mode)) {
            try {
                byte[] decoded = Base64.getDecoder().decode(cipherText);
                return new String(decoded, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        if ("aes".equals(mode) || "aes-gcm".equals(mode)) {
            return decryptAesGcm(cipherText, authProperties.getPassword().getTransportKey());
        }
        return cipherText;
    }

    /**
     * 解析创建用户时的原始密码：若未提供则使用默认密码。
     *
     * @param rawPassword 原始输入密码（可能为密文）
     * @return 明文密码，解析失败返回 null
     */
    public String resolveRawPassword(String rawPassword) {
        if (StringUtils.isBlank(rawPassword)) {
            String fallback = authProperties.getPassword().getDefaultPassword();
            return StringUtils.isBlank(fallback) ? null : fallback;
        }
        return decodeTransportPassword(rawPassword);
    }

    /**
     * 获取默认密码配置。
     *
     * @return 默认密码
     */
    public String getDefaultPassword() {
        return authProperties.getPassword().getDefaultPassword();
    }

    /**
     * 校验密码强度（可配置开关与规则）。
     *
     * @param rawPassword 明文密码
     * @return true 表示通过校验
     */
    public boolean isStrongPassword(String rawPassword) {
        if (!authProperties.getPassword().isStrongCheckEnabled()) {
            return true;
        }
        if (rawPassword == null) {
            return false;
        }
        int minLength = authProperties.getPassword().getStrongMinLength();
        if (minLength > 0 && rawPassword.length() < minLength) {
            return false;
        }
        String pattern = authProperties.getPassword().getStrongPattern();
        if (StringUtils.isNotBlank(pattern)) {
            return Pattern.matches(pattern, rawPassword);
        }
        return hasLower(rawPassword) && hasUpper(rawPassword) && hasDigit(rawPassword) && hasSpecial(rawPassword);
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

    private String normalizeTransportMode(String mode) {
        return mode == null ? "plain" : mode.trim().toLowerCase();
    }

    private String decryptAesGcm(String cipherText, String base64Key) {
        if (StringUtils.isBlank(base64Key)) {
            return null;
        }
        String[] parts = cipherText.split(":", 2);
        if (parts.length != 2) {
            return null;
        }
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encrypted = Base64.getDecoder().decode(parts[1]);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            byte[] plain = cipher.doFinal(encrypted);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean hasLower(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= 'a' && c <= 'z') {
                return true;
            }
        }
        return false;
    }

    private boolean hasUpper(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                return true;
            }
        }
        return false;
    }

    private boolean hasDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSpecial(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return true;
            }
        }
        return false;
    }
}
