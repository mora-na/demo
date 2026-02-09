package com.example.demo.auth.store;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class CaptchaStore {

    private static final String CAPTCHA_KEY_PREFIX = "auth:captcha:";

    private final StringRedisTemplate stringRedisTemplate;

    public CaptchaStore(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void save(String captchaId, String code, long expireAtSeconds) {
        if (captchaId == null || code == null) {
            return;
        }
        long ttlSeconds = Math.max(1, expireAtSeconds - Instant.now().getEpochSecond());
        stringRedisTemplate.opsForValue()
                .set(buildKey(captchaId), code, Duration.ofSeconds(ttlSeconds));
    }

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

    private String buildKey(String captchaId) {
        return CAPTCHA_KEY_PREFIX + captchaId;
    }
}
