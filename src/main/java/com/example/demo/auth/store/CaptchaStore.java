package com.example.demo.auth.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * 验证码存储，基于 Redis 保存验证码与过期时间。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class CaptchaStore {

    private static final String CAPTCHA_KEY_PREFIX = "auth:captcha:";

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造函数，注入 Redis 模板。
     *
     * @param stringRedisTemplate Redis 模板
     */
    public CaptchaStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
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
        stringRedisTemplate.opsForValue()
                .set(buildKey(captchaId), code, Duration.ofSeconds(ttlSeconds));
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
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null) {
            return false;
        }
        stringRedisTemplate.delete(key);
        return stored.equalsIgnoreCase(code);
    }

    /**
     * 构建 Redis Key。
     *
     * @param captchaId 验证码 ID
     * @return Redis Key
     */
    private String buildKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }
}
