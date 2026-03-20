package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.UserProfileResponse;
import com.example.demo.common.cache.CacheTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录用户画像缓存（跨节点共享）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/20
 */
@Component
public class UserProfileCache {

    private static final String USER_PROFILE_PREFIX = "auth:user:profile:";

    private final AuthProperties authProperties;
    private final CacheTool cacheTool;
    private final ObjectMapper objectMapper;

    public UserProfileCache(AuthProperties authProperties,
                            CacheTool cacheTool,
                            ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.cacheTool = cacheTool;
        this.objectMapper = objectMapper;
    }

    public UserProfileResponse get(Long userId) {
        if (userId == null || !isEnabled()) {
            return null;
        }
        Object value = cacheTool.get(buildKey(userId));
        if (value == null) {
            return null;
        }
        if (value instanceof UserProfileResponse) {
            return (UserProfileResponse) value;
        }
        try {
            return objectMapper.convertValue(value, UserProfileResponse.class);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    public void put(Long userId, UserProfileResponse profile) {
        if (userId == null || profile == null || !isEnabled()) {
            return;
        }
        int ttlSeconds = authProperties.getCache().getUserProfileTtlSeconds();
        if (ttlSeconds <= 0) {
            return;
        }
        cacheTool.set(buildKey(userId), profile, Duration.ofSeconds(ttlSeconds));
    }

    public void invalidate(Long userId) {
        if (userId == null) {
            return;
        }
        cacheTool.delete(buildKey(userId));
    }

    private boolean isEnabled() {
        AuthProperties.Cache cacheConfig = authProperties.getCache();
        return cacheConfig != null
                && cacheConfig.isUserProfileCacheEnabled()
                && cacheConfig.getUserProfileTtlSeconds() > 0
                && cacheConfig.getUserProfileMaxSize() > 0;
    }

    private String buildKey(Long userId) {
        return USER_PROFILE_PREFIX + userId;
    }
}
