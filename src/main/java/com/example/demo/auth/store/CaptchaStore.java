package com.example.demo.auth.store;

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

    private static final String CAPTCHA_KEY_PREFIX = "auth:captcha:";

    private final CacheTool cacheTool;

    /**
     * 构造函数，注入缓存工具。
     *
     * @param cacheTool 缓存工具
     */
    public CaptchaStore(CacheTool cacheTool) {
        this.cacheTool = cacheTool;
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
     * 构建缓存 Key。
     *
     * @param captchaId 验证码 ID
     * @return 缓存 Key
     */
    private String buildKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }
}
