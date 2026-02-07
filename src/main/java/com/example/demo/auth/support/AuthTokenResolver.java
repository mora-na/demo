package com.example.demo.auth.support;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public final class AuthTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private AuthTokenResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isNotBlank(authorization)) {
            if (authorization.startsWith(BEARER_PREFIX)) {
                return authorization.substring(BEARER_PREFIX.length());
            }
            return authorization;
        }
        String token = request.getHeader("X-Auth-Token");
        if (StringUtils.isNotBlank(token)) {
            return token;
        }
        return request.getParameter("token");
    }
}
