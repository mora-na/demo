package com.example.demo.auth.store;

import com.example.demo.auth.config.AuthProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class CaptchaStore {

    private final AuthProperties authProperties;
    private final Map<String, CaptchaRecord> store = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupMillis = new AtomicLong(0L);

    public CaptchaStore(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public void save(String captchaId, String code, long expireAtSeconds) {
        store.put(captchaId, new CaptchaRecord(code, expireAtSeconds));
        maybeCleanup();
        enforceMaxEntries();
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

    private void maybeCleanup() {
        int intervalSeconds = authProperties.getCaptcha().getCleanupIntervalSeconds();
        if (intervalSeconds <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long last = lastCleanupMillis.get();
        if (now - last < intervalSeconds * 1000L) {
            return;
        }
        if (!lastCleanupMillis.compareAndSet(last, now)) {
            return;
        }
        cleanupExpired();
    }

    private void cleanupExpired() {
        long now = Instant.now().getEpochSecond();
        store.entrySet().removeIf(entry -> entry.getValue().getExpireAtSeconds() < now);
    }

    private void enforceMaxEntries() {
        int maxEntries = authProperties.getCaptcha().getMaxEntries();
        if (maxEntries <= 0 || store.size() <= maxEntries) {
            return;
        }
        cleanupExpired();
        int toRemove = store.size() - maxEntries;
        if (toRemove <= 0) {
            return;
        }
        Iterator<String> iterator = store.keySet().iterator();
        while (toRemove > 0 && iterator.hasNext()) {
            iterator.next();
            iterator.remove();
            toRemove--;
        }
    }

    @Data
    @AllArgsConstructor
    private static class CaptchaRecord {
        private String code;
        private long expireAtSeconds;
    }
}
