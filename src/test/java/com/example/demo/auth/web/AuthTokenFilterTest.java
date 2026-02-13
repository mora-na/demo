package com.example.demo.auth.web;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.TokenService;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.datascope.service.DataScopeResolver;
import com.example.demo.user.entity.SysUser;
import com.example.demo.user.service.SysUserService;
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
        SysUserService userService = Mockito.mock(SysUserService.class);
        DataScopeResolver dataScopeResolver = Mockito.mock(DataScopeResolver.class);
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        I18nService i18nService = Mockito.mock(I18nService.class);
        AuthTokenFilter filter = new AuthTokenFilter(properties, commonExcludePaths, tokenService, userService, dataScopeResolver, i18nService);

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
        SysUserService userService = Mockito.mock(SysUserService.class);
        DataScopeResolver dataScopeResolver = Mockito.mock(DataScopeResolver.class);
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        I18nService i18nService = Mockito.mock(I18nService.class);
        when(i18nService.getMessage(any(javax.servlet.http.HttpServletRequest.class), anyString()))
                .thenReturn("token is missing");
        AuthTokenFilter filter = new AuthTokenFilter(properties, commonExcludePaths, tokenService, userService, dataScopeResolver, i18nService);

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
        SysUserService userService = Mockito.mock(SysUserService.class);
        DataScopeResolver dataScopeResolver = Mockito.mock(DataScopeResolver.class);
        SysUser dbUser = new SysUser();
        dbUser.setId(1L);
        dbUser.setUserName("alice");
        dbUser.setNickName("Alice");
        dbUser.setStatus(SysUser.STATUS_ENABLED);
        when(userService.getById(1L)).thenReturn(dbUser);
        CommonExcludePathsProperties commonExcludePaths = new CommonExcludePathsProperties();
        when(dataScopeResolver.resolve(dbUser)).thenReturn(new DataScopeResolver.DataScopeResult("ALL", null));
        I18nService i18nService = Mockito.mock(I18nService.class);
        AuthTokenFilter filter = new AuthTokenFilter(properties, commonExcludePaths, tokenService, userService, dataScopeResolver, i18nService);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
