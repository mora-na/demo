package com.example.demo.auth.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaptchaStore {

    private final Map<String, CaptchaRecord> store = new ConcurrentHashMap<>();

    public void save(String captchaId, String code, long expireAtSeconds) {
        store.put(captchaId, new CaptchaRecord(code, expireAtSeconds));
    }

    public boolean verifyAndRemove(String captchaId, String code) {
        if (captchaId == null) {
            return false;
        }
        CaptchaRecord record = store.remove(captchaId);
        if (record == null) {
            return false;
        }
        long now = Instant.now().getEpochSecond();
        if (record.getExpireAtSeconds() < now) {
            return false;
        }
        return record.getCode().equalsIgnoreCase(code);
    }

    @Data
    @AllArgsConstructor
    private static class CaptchaRecord {
        private String code;
        private long expireAtSeconds;
    }
}
