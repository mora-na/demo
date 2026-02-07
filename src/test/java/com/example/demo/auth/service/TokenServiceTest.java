package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.store.TokenStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Test
    void issueAndVerifyToken() {
        AuthProperties properties = new AuthProperties();
        properties.getJwt().setSecret("test-secret");
        properties.getJwt().setTtlSeconds(60);
        TokenStore store = new TokenStore();
        TokenService service = new TokenService(properties, store);

        AuthUser user = new AuthUser(1L, "alice", "Alice");
        String token = service.issueToken(user).getToken();
        assertNotNull(token);

        AuthUser verified = service.verifyToken(token);
        assertNotNull(verified);
        assertEquals(user.getUserName(), verified.getUserName());
    }

    @Test
    void verifyToken_returnsNullWhenExpired() {
        AuthProperties properties = new AuthProperties();
        properties.getJwt().setSecret("test-secret");
        properties.getJwt().setTtlSeconds(-1);
        TokenStore store = new TokenStore();
        TokenService service = new TokenService(properties, store);

        AuthUser user = new AuthUser(2L, "bob", "Bob");
        String token = service.issueToken(user).getToken();
        assertNull(service.verifyToken(token));
        assertNull(store.get(token));
    }
}
