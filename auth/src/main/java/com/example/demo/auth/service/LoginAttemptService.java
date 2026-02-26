package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.support.ClientIpResolver;
import com.example.demo.common.cache.CacheTool;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 登录失败限制，基于缓存计数器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final CacheTool cacheTool;
    private final AuthProperties authProperties;
    private final AuthConstants authConstants;
    private final ClientIpResolver clientIpResolver;

    /**
     * Check whether the account is locked.
     *
     * @param userName username
     * @return true when locked
     */
    public boolean isLocked(String userName) {
        return isLocked(userName, null);
    }

    /**
     * Check whether the account is locked.
     *
     * @param userName username
     * @param request  HTTP request (optional, for IP-aware keys)
     * @return true when locked
     */
    public boolean isLocked(String userName, HttpServletRequest request) {
        if (isDisabled()) {
            return false;
        }
        String identity = resolveIdentity(userName, request);
        if (identity == null) {
            return false;
        }
        return cacheTool.hasKey(buildLockKey(identity));
    }

    /**
     * Return remaining lock seconds.
     *
     * @param userName username
     * @return remaining seconds, 0 when not locked
     */
    public long getRemainingLockSeconds(String userName) {
        return getRemainingLockSeconds(userName, null);
    }

    /**
     * Return remaining lock seconds.
     *
     * @param userName username
     * @param request  HTTP request (optional, for IP-aware keys)
     * @return remaining seconds, 0 when not locked
     */
    public long getRemainingLockSeconds(String userName, HttpServletRequest request) {
        if (isDisabled()) {
            return 0;
        }
        String identity = resolveIdentity(userName, request);
        if (identity == null) {
            return 0;
        }
        long remaining = cacheTool.getExpire(buildLockKey(identity), TimeUnit.SECONDS);
        if (remaining > 0) {
            return remaining;
        }
        if (remaining == -1) {
            int lockSeconds = authProperties.getLoginLimit() == null ? 0 : authProperties.getLoginLimit().getLockSeconds();
            return Math.max(0, lockSeconds);
        }
        return 0;
    }

    /**
     * Record a login failure and lock when the threshold is reached.
     *
     * @param userName username
     */
    public void recordFailure(String userName) {
        recordFailure(userName, null);
    }

    /**
     * Record a login failure and lock when the threshold is reached.
     *
     * @param userName username
     * @param request  HTTP request (optional, for IP-aware keys)
     */
    public void recordFailure(String userName, HttpServletRequest request) {
        if (isDisabled()) {
            return;
        }
        String identity = resolveIdentity(userName, request);
        if (identity == null) {
            return;
        }
        AuthProperties.LoginLimit limit = authProperties.getLoginLimit();
        int maxErrors = Math.max(1, limit.getMaxErrors());
        int lockSeconds = Math.max(1, limit.getLockSeconds());
        int windowSeconds = Math.max(1, limit.getWindowSeconds());
        String lockKey = buildLockKey(identity);
        if (cacheTool.hasKey(lockKey)) {
            return;
        }
        String failKey = buildFailKey(identity);
        Duration ttl = Duration.ofSeconds(windowSeconds);
        Boolean created = cacheTool.setIfAbsent(failKey, "1", ttl);
        long count = 1;
        if (!Boolean.TRUE.equals(created)) {
            Long updated = cacheTool.increment(failKey);
            count = updated == null ? 0 : updated;
        }
        if (count >= maxErrors) {
            cacheTool.setString(lockKey, "1", Duration.ofSeconds(lockSeconds));
            cacheTool.delete(failKey);
            return;
        }
        // Sliding window: refresh TTL on each failure.
        cacheTool.expire(failKey, windowSeconds, TimeUnit.SECONDS);
    }

    /**
     * Clear failure counter and lock state.
     *
     * @param userName username
     */
    public void clearFailures(String userName) {
        clearFailures(userName, null);
    }

    /**
     * Clear failure counter and lock state.
     *
     * @param userName username
     * @param request  HTTP request (optional, for IP-aware keys)
     */
    public void clearFailures(String userName, HttpServletRequest request) {
        if (isDisabled()) {
            return;
        }
        String identity = resolveIdentity(userName, request);
        if (identity == null) {
            return;
        }
        cacheTool.delete(buildFailKey(identity));
        cacheTool.delete(buildLockKey(identity));
    }

    private boolean isDisabled() {
        AuthProperties.LoginLimit limit = authProperties.getLoginLimit();
        return limit == null
                || !limit.isEnabled()
                || limit.getMaxErrors() <= 0
                || limit.getLockSeconds() <= 0
                || limit.getWindowSeconds() <= 0;
    }

    private String normalizeUserName(String userName) {
        String normalized = StringUtils.trimToNull(userName);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String resolveIdentity(String userName, HttpServletRequest request) {
        String normalized = normalizeUserName(userName);
        if (normalized == null) {
            return null;
        }
        String keyMode = normalizeKeyMode();
        AuthConstants.LoginAttempt constants = authConstants.getLoginAttempt();
        String ip = clientIpResolver.resolve(request);
        if (normalizeModeValue(constants.getModeIp()).equals(keyMode)) {
            return StringUtils.isBlank(ip) ? normalized : ip;
        }
        if (normalizeModeValue(constants.getModeIpUser()).equals(keyMode)
                || normalizeModeValue(constants.getModeUserIp()).equals(keyMode)) {
            return StringUtils.isBlank(ip) ? normalized : ip + ":" + normalized;
        }
        return normalized;
    }

    private String normalizeKeyMode() {
        AuthProperties.LoginLimit limit = authProperties.getLoginLimit();
        String mode = limit == null ? null : limit.getKeyMode();
        String fallback = authConstants.getLoginAttempt().getModeFallback();
        return mode == null ? normalizeModeValue(fallback) : mode.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveClientIp(HttpServletRequest request) {
        return clientIpResolver.resolve(request);
    }

    private String buildFailKey(String identity) {
        return authConstants.getLoginAttempt().getFailKeyPrefix() + identity;
    }

    private String buildLockKey(String identity) {
        return authConstants.getLoginAttempt().getLockKeyPrefix() + identity;
    }

    private String normalizeModeValue(String mode) {
        return mode == null ? AuthConstants.LoginAttempt.DEFAULT_MODE_FALLBACK : mode.trim().toLowerCase(Locale.ROOT);
    }
}
