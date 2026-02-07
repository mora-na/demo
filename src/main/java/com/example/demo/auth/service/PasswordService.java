package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final AuthProperties authProperties;
    private final BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();

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

    private String md5(String rawPassword) {
        String salt = authProperties.getPassword().getSalt();
        String value = (salt == null ? "" : salt) + rawPassword;
        return DigestUtils.md5DigestAsHex(value.getBytes(StandardCharsets.UTF_8));
    }

    private String normalizeMode(String mode) {
        return mode == null ? "plain" : mode.trim().toLowerCase();
    }
}
