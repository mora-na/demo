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
        AuthUser user = new AuthUser(1L, "alice", "Alice");
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
        AuthUser user = new AuthUser(2L, "bob", "Bob");
        long expiredAt = Instant.now().getEpochSecond() - 1;

        store.save("expired", user, expiredAt);
        assertNull(store.get("expired"));
    }
}
