package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.identity.api.dto.IdentityDataScopeProfileDTO;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 轻量用户状态缓存，避免每次请求都访问 IdentityReadFacade。
 */
@Component
public class AuthUserStatusCache {

    private final AuthProperties authProperties;
    private final Cache<Long, CacheEntry> cache;
    private final Cache<Long, ProfileCacheEntry> profileCache;

    public AuthUserStatusCache(AuthProperties authProperties) {
        this.authProperties = authProperties;
        int ttlSeconds = Math.max(10, authProperties.getCache().getUserStatusTtlSeconds());
        int maxSize = authProperties.getCache().getUserStatusMaxSize();
        if (maxSize <= 0) {
            maxSize = 5000;
        }
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
                .build();
        int profileTtlSeconds = Math.max(10, authProperties.getCache().getDataScopeProfileTtlSeconds());
        int profileMaxSize = authProperties.getCache().getDataScopeProfileMaxSize();
        if (profileMaxSize <= 0) {
            profileMaxSize = 5000;
        }
        this.profileCache = Caffeine.newBuilder()
                .maximumSize(profileMaxSize)
                .expireAfterWrite(profileTtlSeconds, TimeUnit.SECONDS)
                .build();
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
        if (userId == null) {
            return null;
        }
        if (!isEnabled()) {
            return null;
        }
        CacheEntry entry = cache.getIfPresent(userId);
        if (entry == null) {
            return null;
        }
        if (entry.expireAtSeconds < Instant.now().getEpochSecond()) {
            cache.invalidate(userId);
            return null;
        }
        return entry.negative ? null : entry.user;
    }

    public IdentityDataScopeProfileDTO getProfile(Long userId) {
        if (userId == null) {
            return null;
        }
        if (!isProfileEnabled()) {
            return null;
        }
        ProfileCacheEntry entry = profileCache.getIfPresent(userId);
        if (entry == null) {
            return null;
        }
        if (entry.expireAtSeconds < Instant.now().getEpochSecond()) {
            profileCache.invalidate(userId);
            return null;
        }
        return entry.profile;
    }

    public void put(Long userId, IdentityUserDTO user, long ttlSeconds) {
        if (userId == null || user == null || ttlSeconds <= 0) {
            return;
        }
        if (!isEnabled()) {
            return;
        }
        long expireAt = Instant.now().getEpochSecond() + ttlSeconds;
        cache.put(userId, new CacheEntry(user, expireAt, false));
    }

    public void putProfile(Long userId, IdentityDataScopeProfileDTO profile, long ttlSeconds) {
        if (userId == null || profile == null || ttlSeconds <= 0) {
            return;
        }
        if (!isProfileEnabled()) {
            return;
        }
        long expireAt = Instant.now().getEpochSecond() + ttlSeconds;
        profileCache.put(userId, new ProfileCacheEntry(profile, expireAt));
    }

    public void putNegative(Long userId, long ttlSeconds) {
        if (userId == null || ttlSeconds <= 0) {
            return;
        }
        if (!negativeEnabled()) {
            return;
        }
        long expireAt = Instant.now().getEpochSecond() + ttlSeconds;
        cache.put(userId, new CacheEntry(null, expireAt, true));
    }

    public void invalidate(Long userId) {
        if (userId == null) {
            return;
        }
        if (!isEnabled()) {
            return;
        }
        cache.invalidate(userId);
    }

    public void invalidateProfile(Long userId) {
        if (userId == null) {
            return;
        }
        if (!isProfileEnabled()) {
            return;
        }
        profileCache.invalidate(userId);
    }

    private static final class CacheEntry {
        private final IdentityUserDTO user;
        private final long expireAtSeconds;
        private final boolean negative;

        private CacheEntry(IdentityUserDTO user, long expireAtSeconds, boolean negative) {
            this.user = user;
            this.expireAtSeconds = expireAtSeconds;
            this.negative = negative;
        }
    }

    private static final class ProfileCacheEntry {
        private final IdentityDataScopeProfileDTO profile;
        private final long expireAtSeconds;

        private ProfileCacheEntry(IdentityDataScopeProfileDTO profile, long expireAtSeconds) {
            this.profile = profile;
            this.expireAtSeconds = expireAtSeconds;
        }
    }
}
