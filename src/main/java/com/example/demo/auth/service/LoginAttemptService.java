package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Login failure limiter backed by Redis counters.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private static final String FAIL_KEY_PREFIX = "auth:login:fail:";
    private static final String LOCK_KEY_PREFIX = "auth:login:lock:";

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

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
        return stringRedisTemplate.hasKey(buildLockKey(identity));
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
        long remaining = stringRedisTemplate.getExpire(buildLockKey(identity), TimeUnit.SECONDS);
        if (remaining > 0) {
            return remaining;
        }
        int lockSeconds = authProperties.getLoginLimit() == null ? 0 : authProperties.getLoginLimit().getLockSeconds();
        return Math.max(0, lockSeconds);
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
        if (stringRedisTemplate.hasKey(lockKey)) {
            return;
        }
        String failKey = buildFailKey(identity);
        Duration ttl = Duration.ofSeconds(windowSeconds);
        Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(failKey, "1", ttl);
        long count = 1;
        if (!Boolean.TRUE.equals(created)) {
            Long updated = stringRedisTemplate.opsForValue().increment(failKey);
            count = updated == null ? 0 : updated;
        }
        if (count >= maxErrors) {
            stringRedisTemplate.opsForValue().set(lockKey, "1", Duration.ofSeconds(lockSeconds));
            stringRedisTemplate.delete(failKey);
            return;
        }
        // Sliding window: refresh TTL on each failure.
        stringRedisTemplate.expire(failKey, windowSeconds, TimeUnit.SECONDS);
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
        stringRedisTemplate.delete(buildFailKey(identity));
        stringRedisTemplate.delete(buildLockKey(identity));
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
        return StringUtils.trimToNull(userName);
    }

    private String resolveIdentity(String userName, HttpServletRequest request) {
        String normalized = normalizeUserName(userName);
        if (normalized == null) {
            return null;
        }
        String keyMode = normalizeKeyMode();
        String ip = resolveClientIp(request);
        if ("ip".equals(keyMode)) {
            return StringUtils.isBlank(ip) ? normalized : ip;
        }
        if ("ip-user".equals(keyMode) || "user-ip".equals(keyMode)) {
            return StringUtils.isBlank(ip) ? normalized : ip + ":" + normalized;
        }
        return normalized;
    }

    private String normalizeKeyMode() {
        AuthProperties.LoginLimit limit = authProperties.getLoginLimit();
        String mode = limit == null ? null : limit.getKeyMode();
        return mode == null ? "user" : mode.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.trimToNull(remoteAddr);
    }

    private String buildFailKey(String identity) {
        return FAIL_KEY_PREFIX + identity;
    }

    private String buildLockKey(String identity) {
        return LOCK_KEY_PREFIX + identity;
    }
}
