package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.store.TokenStore;
import com.example.demo.common.cache.CacheProperties;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.cache.MemoryCacheStore;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    @Test
    void issueAndVerifyToken() {
        AuthProperties properties = new AuthProperties();
        properties.getJwt().setSecret("test-secret");
        properties.getJwt().setTtlSeconds(60);
        CacheProperties.Memory memory = new CacheProperties.Memory();
        memory.setCleanupIntervalSeconds(0);
        memory.setMaximumWeightMb(0);
        TokenStore store = new TokenStore(new CacheTool(new MemoryCacheStore(memory)));
        TokenService service = new TokenService(properties, store);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Alice");
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
        CacheProperties.Memory memory = new CacheProperties.Memory();
        memory.setCleanupIntervalSeconds(0);
        memory.setMaximumWeightMb(0);
        TokenStore store = new TokenStore(new CacheTool(new MemoryCacheStore(memory)));
        TokenService service = new TokenService(properties, store);

        AuthUser user = new AuthUser();
        user.setId(2L);
        user.setUserName("bob");
        user.setNickName("Bob");
        String token = service.issueToken(user).getToken();
        assertNull(service.verifyToken(token));
        assertNull(store.get(token));
    }
}
