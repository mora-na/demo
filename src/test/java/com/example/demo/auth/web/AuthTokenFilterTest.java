package com.example.demo.auth.web;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.TokenService;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthTokenFilterTest {

    @Test
    void excludedPath_skipsFilter() throws Exception {
        AuthProperties properties = new AuthProperties();
        properties.getFilter().setEnabled(true);
        properties.getFilter().setExcludePaths(Collections.singletonList("/auth/**"));
        TokenService tokenService = Mockito.mock(TokenService.class);
        UserService userService = Mockito.mock(UserService.class);
        AuthTokenFilter filter = new AuthTokenFilter(properties, tokenService, userService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(tokenService, never()).verifyToken(Mockito.anyString());
    }

    @Test
    void missingToken_returnsUnauthorized() throws Exception {
        AuthProperties properties = new AuthProperties();
        properties.getFilter().setEnabled(true);
        properties.getFilter().setExcludePaths(Collections.emptyList());
        TokenService tokenService = Mockito.mock(TokenService.class);
        UserService userService = Mockito.mock(UserService.class);
        AuthTokenFilter filter = new AuthTokenFilter(properties, tokenService, userService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void validToken_allowsRequest() throws Exception {
        AuthProperties properties = new AuthProperties();
        properties.getFilter().setEnabled(true);
        properties.getFilter().setExcludePaths(Collections.emptyList());
        TokenService tokenService = Mockito.mock(TokenService.class);
        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Alice");
        when(tokenService.verifyToken("token")).thenReturn(user);
        UserService userService = Mockito.mock(UserService.class);
        User dbUser = new User();
        dbUser.setId(1L);
        dbUser.setUserName("alice");
        dbUser.setNickName("Alice");
        dbUser.setStatus(User.STATUS_ENABLED);
        when(userService.getById(1L)).thenReturn(dbUser);
        AuthTokenFilter filter = new AuthTokenFilter(properties, tokenService, userService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
