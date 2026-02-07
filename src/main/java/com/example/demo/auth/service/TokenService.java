package com.example.demo.auth.service;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.LoginResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.store.TokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthProperties authProperties;
    private final TokenStore tokenStore;

    public LoginResponse issueToken(AuthUser user) {
        long nowSeconds = Instant.now().getEpochSecond();
        long expireAt = nowSeconds + authProperties.getJwt().getTtlSeconds();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", user.getUserName());
        payload.put("uid", user.getId());
        payload.put("iat", nowSeconds);
        payload.put("exp", expireAt);
        payload.put("jti", UUID.randomUUID().toString());
        String token = JWTUtil.createToken(payload, getSecret());
        tokenStore.save(token, user, expireAt);
        return new LoginResponse(token, TOKEN_TYPE, expireAt, user);
    }

    public AuthUser verifyToken(String token) {
        if (token == null) {
            return null;
        }
        if (!JWTUtil.verify(token, getSecret())) {
            return null;
        }
        JWT jwt = JWTUtil.parseToken(token);
        Long exp = getLongClaim(jwt, "exp");
        long now = Instant.now().getEpochSecond();
        if (exp == null || exp < now) {
            tokenStore.revoke(token);
            return null;
        }
        TokenStore.TokenRecord record = tokenStore.get(token);
        return record == null ? null : record.getUser();
    }

    public void revoke(String token) {
        tokenStore.revoke(token);
    }

    private byte[] getSecret() {
        return authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }

    private Long getLongClaim(JWT jwt, String key) {
        Object value = jwt.getPayload(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
