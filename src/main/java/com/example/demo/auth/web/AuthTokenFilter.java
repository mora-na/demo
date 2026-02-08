package com.example.demo.auth.web;

import com.alibaba.fastjson2.JSON;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.support.AuthTokenResolver;
import com.example.demo.common.model.CommonResult;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthProperties authProperties;
    private final TokenService tokenService;
    private final UserService userService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!authProperties.getFilter().isEnabled()) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        for (String pattern : authProperties.getFilter().getExcludePaths()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = AuthTokenResolver.resolve(request);
        if (StringUtils.isBlank(token)) {
            writeUnauthorized(response, "token is missing");
            return;
        }
        AuthUser user = tokenService.verifyToken(token);
        if (user == null) {
            writeUnauthorized(response, "token is invalid or expired");
            return;
        }
        if (user.getId() == null) {
            writeUnauthorized(response, "user is invalid");
            return;
        }
        User dbUser = userService.getById(user.getId());
        if (dbUser == null) {
            writeUnauthorized(response, "user not found");
            return;
        }
        if (dbUser.getStatus() != null && dbUser.getStatus().equals(User.STATUS_DISABLED)) {
            writeForbidden(response, "user is disabled");
            return;
        }
        user.setUserName(dbUser.getUserName());
        user.setNickName(dbUser.getNickName());
        user.setDataScopeType(dbUser.getDataScopeType());
        user.setDataScopeValue(dbUser.getDataScopeValue());
        AuthContext.set(user);
        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_UNAUTHORIZED, message);
        response.getWriter().write(JSON.toJSONString(result));
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_FORBIDDEN, message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
