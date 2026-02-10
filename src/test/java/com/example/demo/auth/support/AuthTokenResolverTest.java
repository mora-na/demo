package com.example.demo.auth.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthTokenResolverTest {

    @Test
    void resolve_prefersAuthorizationBearer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token-123");
        assertEquals("token-123", AuthTokenResolver.resolve(request));
    }

    @Test
    void resolve_usesAuthorizationRawWhenNoBearer() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "raw-token");
        assertEquals("raw-token", AuthTokenResolver.resolve(request));
    }

    @Test
    void resolve_fallsBackToHeaderOnly() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Auth-Token", "header-token");
        assertEquals("header-token", AuthTokenResolver.resolve(request));

        MockHttpServletRequest requestWithParam = new MockHttpServletRequest();
        requestWithParam.setParameter("token", "param-token");
        assertNull(AuthTokenResolver.resolve(requestWithParam));
    }

    @Test
    void resolve_returnsNullWhenRequestMissing() {
        assertNull(AuthTokenResolver.resolve(null));
    }
}
