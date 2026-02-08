package com.example.demo.auth.store;

import com.example.demo.auth.model.AuthUser;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TokenStoreTest {

    @Test
    void saveGetRevoke() {
        TokenStore store = new TokenStore();
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Alice");
        long expireAt = Instant.now().getEpochSecond() + 10;

        store.save("token", user, expireAt);
        TokenStore.TokenRecord record = store.get("token");
        assertEquals(user, record.getUser());

        store.revoke("token");
        assertNull(store.get("token"));
    }

    @Test
    void get_returnsNullWhenExpired() {
        TokenStore store = new TokenStore();
        AuthUser user = new AuthUser();
        user.setId(2L);
        user.setUserName("bob");
        user.setNickName("Bob");
        long expiredAt = Instant.now().getEpochSecond() - 1;

        store.save("expired", user, expiredAt);
        assertNull(store.get("expired"));
    }
}
