package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.identity.api.dto.IdentityDataScopeProfileDTO;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 轻量用户状态缓存（跨节点共享）。
 */
@Component
public class AuthUserStatusCache {

    private static final String USER_STATUS_PREFIX = "auth:user:status:";
    private static final String USER_FIELD = "user";
    private static final String PROFILE_FIELD = "profile";
    private static final String NEGATIVE_MARKER = "__NEGATIVE__";

    private final AuthProperties authProperties;
    private final CacheTool cacheTool;
    private final ObjectMapper objectMapper;

    public AuthUserStatusCache(AuthProperties authProperties,
                               CacheTool cacheTool,
                               ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.cacheTool = cacheTool;
        this.objectMapper = objectMapper;
    }

    private boolean isEnabled() {
        AuthProperties.Cache cacheConfig = authProperties.getCache();
        return cacheConfig != null
                && cacheConfig.isUserStatusCacheEnabled()
                && cacheConfig.getUserStatusTtlSeconds() > 0
                && cacheConfig.getUserStatusMaxSize() > 0;
    }

    private boolean negativeEnabled() {
        AuthProperties.Cache cacheConfig = authProperties.getCache();
        return isEnabled() && cacheConfig.getUserStatusNegativeTtlSeconds() > 0;
    }

    private boolean isProfileEnabled() {
        AuthProperties.Cache cacheConfig = authProperties.getCache();
        return cacheConfig != null
                && cacheConfig.isDataScopeProfileCacheEnabled()
                && cacheConfig.getDataScopeProfileTtlSeconds() > 0
                && cacheConfig.getDataScopeProfileMaxSize() > 0;
    }

    public IdentityUserDTO get(Long userId) {
        if (userId == null || !isEnabled()) {
            return null;
        }
        Object value = cacheTool.hget(buildUserKey(userId), USER_FIELD);
        if (value == null) {
            return null;
        }
        if (value instanceof String && NEGATIVE_MARKER.equals(value)) {
            return null;
        }
        return convert(value, IdentityUserDTO.class);
    }

    public IdentityDataScopeProfileDTO getProfile(Long userId) {
        if (userId == null || !isProfileEnabled()) {
            return null;
        }
        Object value = cacheTool.hget(buildUserKey(userId), PROFILE_FIELD);
        if (value == null) {
            return null;
        }
        return convert(value, IdentityDataScopeProfileDTO.class);
    }

    public Snapshot getSnapshot(Long userId) {
        if (userId == null) {
            return null;
        }
        boolean userEnabled = isEnabled();
        boolean profileEnabled = isProfileEnabled();
        if (!userEnabled && !profileEnabled) {
            return null;
        }
        List<Object> values = cacheTool.hmget(buildUserKey(userId), USER_FIELD, PROFILE_FIELD);
        if (values == null || values.isEmpty()) {
            return null;
        }
        Object userValue = values.size() > 0 ? values.get(0) : null;
        Object profileValue = values.size() > 1 ? values.get(1) : null;
        IdentityUserDTO user = null;
        IdentityDataScopeProfileDTO profile = null;
        boolean negative = false;
        boolean userPresent = userValue != null;
        boolean profilePresent = profileValue != null;
        if (userEnabled && userValue != null) {
            if (userValue instanceof String && NEGATIVE_MARKER.equals(userValue)) {
                negative = true;
            } else {
                user = convert(userValue, IdentityUserDTO.class);
            }
        }
        if (profileEnabled && profileValue != null) {
            profile = convert(profileValue, IdentityDataScopeProfileDTO.class);
        }
        return new Snapshot(user, profile, userPresent, profilePresent, userEnabled, profileEnabled, negative);
    }

    public void put(Long userId, IdentityUserDTO user, long ttlSeconds) {
        if (userId == null || user == null || ttlSeconds <= 0 || !isEnabled()) {
            return;
        }
        cacheTool.hset(buildUserKey(userId), USER_FIELD, user);
        ensureKeyTtl(userId, ttlSeconds);
    }

    public void putProfile(Long userId, IdentityDataScopeProfileDTO profile, long ttlSeconds) {
        if (userId == null || profile == null || ttlSeconds <= 0 || !isProfileEnabled()) {
            return;
        }
        cacheTool.hset(buildUserKey(userId), PROFILE_FIELD, profile);
        ensureKeyTtl(userId, ttlSeconds);
    }

    public void putNegative(Long userId, long ttlSeconds) {
        if (userId == null || ttlSeconds <= 0 || !negativeEnabled()) {
            return;
        }
        cacheTool.hset(buildUserKey(userId), USER_FIELD, NEGATIVE_MARKER);
        ensureKeyTtl(userId, ttlSeconds);
    }

    public void invalidate(Long userId) {
        if (userId == null) {
            return;
        }
        cacheTool.delete(buildUserKey(userId));
    }

    public void invalidateProfile(Long userId) {
        if (userId == null) {
            return;
        }
        cacheTool.hdel(buildUserKey(userId), PROFILE_FIELD);
    }

    private String buildUserKey(Long userId) {
        return USER_STATUS_PREFIX + userId;
    }

    private <T> T convert(Object value, Class<T> type) {
        if (value == null || type == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        try {
            return objectMapper.convertValue(value, type);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void ensureKeyTtl(Long userId, long ttlSeconds) {
        if (userId == null || ttlSeconds <= 0) {
            return;
        }
        String key = buildUserKey(userId);
        long current = cacheTool.getExpire(key, TimeUnit.SECONDS);
        if (current < ttlSeconds) {
            cacheTool.expire(key, Duration.ofSeconds(ttlSeconds));
        }
    }

    @Data
    @AllArgsConstructor
    public static class Snapshot {
        private IdentityUserDTO user;
        private IdentityDataScopeProfileDTO profile;
        private boolean userPresent;
        private boolean profilePresent;
        private boolean userCacheEnabled;
        private boolean profileCacheEnabled;
        private boolean negative;
    }
}
