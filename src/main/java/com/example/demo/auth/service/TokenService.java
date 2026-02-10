package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.LoginResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.store.TokenStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * 令牌服务，负责 JWT 签发、校验与撤销。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String TOKEN_TYPE = "Bearer";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final AuthProperties authProperties;
    private final TokenStore tokenStore;

    /**
     * 签发 JWT 并写入令牌存储。
     *
     * @param user 认证用户摘要
     * @return 登录响应信息
     */
    public LoginResponse issueToken(AuthUser user) {
        long nowSeconds = Instant.now().getEpochSecond();
        long expireAt = nowSeconds + authProperties.getJwt().getTtlSeconds();
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", user.getUserName());
        payload.put("uid", user.getId());
        payload.put("iat", nowSeconds);
        payload.put("exp", expireAt);
        payload.put("jti", UUID.randomUUID().toString());
        String token = createToken(payload, getSecret());
        tokenStore.save(token, user, expireAt);
        return new LoginResponse(token, TOKEN_TYPE, expireAt, user);
    }

    /**
     * 校验 JWT 有效性并从存储中读取用户信息。
     *
     * @param token JWT 字符串
     * @return 认证用户摘要，校验失败返回 null
     */
    public AuthUser verifyToken(String token) {
        if (token == null) {
            return null;
        }
        Map<String, Object> payload = parseAndVerify(token, getSecret());
        if (payload == null) {
            return null;
        }
        Long exp = getLongClaim(payload, "exp");
        long now = Instant.now().getEpochSecond();
        if (exp == null || exp < now) {
            tokenStore.revoke(token);
            return null;
        }
        TokenStore.TokenRecord record = tokenStore.get(token);
        return record == null ? null : record.getUser();
    }

    /**
     * 撤销令牌并从存储中移除。
     *
     * @param token JWT 字符串
     */
    public void revoke(String token) {
        tokenStore.revoke(token);
    }

    /**
     * 获取 JWT 签名密钥字节数组。
     *
     * @return 密钥字节数组
     */
    private byte[] getSecret() {
        return authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 读取并转换 JWT 中的数值型 Claim。
     *
     * @param key Claim 键名
     * @return Claim 值，解析失败返回 null
     */
    private Long getLongClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
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

    private String createToken(Map<String, Object> payload, byte[] secret) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            String encodedHeader = base64UrlEncode(OBJECT_MAPPER.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(OBJECT_MAPPER.writeValueAsBytes(payload));
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = base64UrlEncode(hmacSha256(signingInput, secret));
            return signingInput + "." + signature;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create JWT", e);
        }
    }

    private Map<String, Object> parseAndVerify(String token, byte[] secret) {
        List<String> parts = splitToken(token);
        if (parts.size() != 3) {
            return null;
        }
        String signingInput = parts.get(0) + "." + parts.get(1);
        byte[] signature = base64UrlDecode(parts.get(2));
        byte[] expected = hmacSha256(signingInput, secret);
        if (!MessageDigest.isEqual(signature, expected)) {
            return null;
        }
        try {
            byte[] payloadBytes = base64UrlDecode(parts.get(1));
            Map<String, Object> payload = OBJECT_MAPPER.readValue(payloadBytes, MAP_TYPE);
            return payload == null ? Collections.emptyMap() : payload;
        } catch (Exception e) {
            return null;
        }
    }

    private List<String> splitToken(String token) {
        int first = token.indexOf('.');
        if (first < 0) {
            return Collections.emptyList();
        }
        int second = token.indexOf('.', first + 1);
        if (second < 0) {
            return Collections.emptyList();
        }
        List<String> parts = new ArrayList<>(3);
        parts.add(token.substring(0, first));
        parts.add(token.substring(first + 1, second));
        parts.add(token.substring(second + 1));
        return parts;
    }

    private String base64UrlEncode(byte[] data) {
        return BASE64_URL_ENCODER.encodeToString(data);
    }

    private byte[] base64UrlDecode(String data) {
        return BASE64_URL_DECODER.decode(data);
    }

    private byte[] hmacSha256(String data, byte[] secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}
