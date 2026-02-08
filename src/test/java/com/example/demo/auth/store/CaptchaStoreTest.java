package com.example.demo.auth.store;

import com.example.demo.auth.config.AuthProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaStoreTest {

    @Test
    void saveAndVerify_successThenRemoved() {
        CaptchaStore store = new CaptchaStore(new AuthProperties());
        long expireAt = Instant.now().getEpochSecond() + 5;
        store.save("id-1", "abcd", expireAt);

        assertTrue(store.verifyAndRemove("id-1", "abcd"));
        assertFalse(store.verifyAndRemove("id-1", "abcd"));
    }

    @Test
    void verify_failsWhenExpiredOrWrongCode() {
        CaptchaStore store = new CaptchaStore(new AuthProperties());
        long expiredAt = Instant.now().getEpochSecond() - 1;
        store.save("id-2", "xyz", expiredAt);

        assertFalse(store.verifyAndRemove("id-2", "xyz"));

        long expireAt = Instant.now().getEpochSecond() + 5;
        store.save("id-3", "good", expireAt);
        assertFalse(store.verifyAndRemove("id-3", "bad"));
    }
}
