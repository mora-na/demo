package com.example.demo.auth.store;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.common.cache.CacheTool;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 验证码存储，基于缓存工具。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class CaptchaStore {

    private final CacheTool cacheTool;
    private final AuthConstants systemConstants;

    /**
     * 构造函数，注入缓存工具。
     *
     * @param cacheTool 缓存工具
     */
    public CaptchaStore(CacheTool cacheTool, AuthConstants systemConstants) {
        this.cacheTool = cacheTool;
        this.systemConstants = systemConstants;
    }

    /**
     * 保存验证码值并设置过期时间。
     *
     * @param captchaId       验证码 ID
     * @param code            验证码值
     * @param expireAtSeconds 过期时间戳（秒）
     */
    public void save(String captchaId, String code, long expireAtSeconds) {
        if (captchaId == null || code == null) {
            return;
        }
        long ttlSeconds = Math.max(1, expireAtSeconds - Instant.now().getEpochSecond());
        cacheTool.set(buildKey(captchaId), code, Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 校验验证码并删除存储。
     *
     * @param captchaId 验证码 ID
     * @param code      输入验证码
     * @return 校验成功返回 true
     */
    public boolean verifyAndRemove(String captchaId, String code) {
        if (captchaId == null) {
            return false;
        }
        if (code == null) {
            return false;
        }
        String key = buildKey(captchaId);
        String stored = cacheTool.get(key, String.class);
        if (stored == null) {
            return false;
        }
        cacheTool.delete(key);
        return stored.equalsIgnoreCase(code);
    }

    /**
     * 限制验证码创建频率，避免无限制堆积。
     *
     * @param maxEntries             窗口内最大创建次数
     * @param cleanupIntervalSeconds 窗口时长（秒）
     * @return true 表示允许创建
     */
    public boolean allowCreate(int maxEntries, int cleanupIntervalSeconds) {
        if (maxEntries <= 0 || cleanupIntervalSeconds <= 0) {
            return true;
        }
        String key = buildCounterKey();
        Long count = cacheTool.increment(key);
        long value = count == null ? 1L : count.longValue();
        if (value == 1L) {
            cacheTool.expire(key, Duration.ofSeconds(cleanupIntervalSeconds));
        }
        return value <= maxEntries;
    }

    /**
     * 构建缓存 Key。
     *
     * @param captchaId 验证码 ID
     * @return 缓存 Key
     */
    private String buildKey(String captchaId) {
        return systemConstants.getCaptcha().getStoreKeyPrefix() + captchaId;
    }

    private String buildCounterKey() {
        return systemConstants.getCaptcha().getStoreKeyPrefix() + "counter";
    }
}
