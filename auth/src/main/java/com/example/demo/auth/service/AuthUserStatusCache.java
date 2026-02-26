package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.identity.api.dto.IdentityDataScopeProfileDTO;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 轻量用户状态缓存（跨节点共享）。
 */
@Component
public class AuthUserStatusCache {

    private static final String USER_STATUS_PREFIX = "auth:user:status:";
    private static final String PROFILE_PREFIX = "auth:user:profile:";
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
        Object value = cacheTool.get(buildUserKey(userId));
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
        Object value = cacheTool.get(buildProfileKey(userId));
        if (value == null) {
            return null;
        }
        return convert(value, IdentityDataScopeProfileDTO.class);
    }

    public void put(Long userId, IdentityUserDTO user, long ttlSeconds) {
        if (userId == null || user == null || ttlSeconds <= 0 || !isEnabled()) {
            return;
        }
        cacheTool.set(buildUserKey(userId), user, Duration.ofSeconds(ttlSeconds));
    }

    public void putProfile(Long userId, IdentityDataScopeProfileDTO profile, long ttlSeconds) {
        if (userId == null || profile == null || ttlSeconds <= 0 || !isProfileEnabled()) {
            return;
        }
        cacheTool.set(buildProfileKey(userId), profile, Duration.ofSeconds(ttlSeconds));
    }

    public void putNegative(Long userId, long ttlSeconds) {
        if (userId == null || ttlSeconds <= 0 || !negativeEnabled()) {
            return;
        }
        cacheTool.set(buildUserKey(userId), NEGATIVE_MARKER, Duration.ofSeconds(ttlSeconds));
    }

    public void invalidate(Long userId) {
        if (userId == null || !isEnabled()) {
            return;
        }
        cacheTool.delete(buildUserKey(userId));
    }

    public void invalidateProfile(Long userId) {
        if (userId == null || !isProfileEnabled()) {
            return;
        }
        cacheTool.delete(buildProfileKey(userId));
    }

    private String buildUserKey(Long userId) {
        return USER_STATUS_PREFIX + userId;
    }

    private String buildProfileKey(Long userId) {
        return PROFILE_PREFIX + userId;
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
}
