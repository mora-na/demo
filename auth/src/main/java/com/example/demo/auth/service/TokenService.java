package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthConstants;
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

    private final ObjectMapper objectMapper;
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    private final AuthProperties authProperties;
    private final TokenStore tokenStore;
    private final AuthConstants systemConstants;

    /**
     * 签发 JWT 并写入令牌存储。
     *
     * @param user 认证用户摘要
     * @return 登录响应信息
     */
    public LoginResponse issueToken(AuthUser user) {
        AuthConstants.Token tokenConstants = systemConstants.getToken();
        long nowSeconds = Instant.now().getEpochSecond();
        long expireAt = nowSeconds + authProperties.getJwt().getTtlSeconds();
        Long userId = user == null ? null : user.getId();
        Map<String, Object> payload = new HashMap<>();
        payload.put(tokenConstants.getJwtClaimSubject(), Objects.requireNonNull(user).getUserName());
        payload.put(tokenConstants.getJwtClaimUserId(), userId);
        payload.put(tokenConstants.getJwtClaimIssuedAt(), nowSeconds);
        payload.put(tokenConstants.getJwtClaimExpiresAt(), expireAt);
        payload.put(tokenConstants.getJwtClaimJwtId(), UUID.randomUUID().toString());
        if (userId != null) {
            long version = tokenStore.getOrInitVersion(userId, authProperties.getJwt().getTtlSeconds());
            payload.put(tokenConstants.getJwtClaimTokenVersion(), version);
        }
        String token = createToken(payload, getSecret());
        tokenStore.save(token, user, expireAt);
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setTokenType(tokenConstants.getTokenType());
        response.setExpiresAt(expireAt);
        response.setUser(user);
        return response;
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
        Long userId = getLongClaim(payload, systemConstants.getToken().getJwtClaimUserId());
        Long exp = getLongClaim(payload, systemConstants.getToken().getJwtClaimExpiresAt());
        long now = Instant.now().getEpochSecond();
        if (exp == null || exp < now) {
            if (userId != null) {
                tokenStore.revoke(userId, token);
            } else {
                tokenStore.revoke(token);
            }
            return null;
        }
        if (userId == null) {
            tokenStore.revoke(token);
            return null;
        }
        TokenStore.TokenSnapshot snapshot = tokenStore.getSnapshot(userId, token);
        if (snapshot == null || snapshot.getRecord() == null) {
            return null;
        }
        Long version = getLongClaim(payload, systemConstants.getToken().getJwtClaimTokenVersion());
        long currentVersion = snapshot.getVersion();
        if (currentVersion > 0) {
            if (version == null || version != currentVersion) {
                tokenStore.revoke(userId, token);
                return null;
            }
        } else if (version != null) {
            tokenStore.setUserTokenVersion(userId, version, authProperties.getJwt().getTtlSeconds());
        }
        return snapshot.getRecord().getUser();
    }

    /**
     * 撤销令牌并从存储中移除。
     *
     * @param token JWT 字符串
     */
    public void revoke(String token) {
        tokenStore.revoke(token);
    }

    public void revoke(Long userId, String token) {
        if (token == null) {
            return;
        }
        if (userId != null) {
            tokenStore.revoke(userId, token);
            return;
        }
        tokenStore.revoke(token);
    }

    /**
     * 撤销用户的全部令牌。
     *
     * @param userId 用户 ID
     */
    public void revokeByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        tokenStore.revokeByUserId(userId, authProperties.getJwt().getTtlSeconds());
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
            AuthConstants.Token tokenConstants = systemConstants.getToken();
            Map<String, Object> header = new HashMap<>();
            header.put(tokenConstants.getJwtHeaderAlgKey(), tokenConstants.getJwtHeaderAlgValue());
            header.put(tokenConstants.getJwtHeaderTypeKey(), tokenConstants.getJwtHeaderTypeValue());
            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
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
            Map<String, Object> payload = objectMapper.readValue(payloadBytes, MAP_TYPE);
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
            String signAlgorithm = systemConstants.getToken().getSignAlgorithm();
            Mac mac = Mac.getInstance(signAlgorithm);
            mac.init(new SecretKeySpec(secret, signAlgorithm));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign JWT", e);
        }
    }
}
