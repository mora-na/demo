package com.example.demo.auth.support;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证令牌解析器，按优先级从请求头或参数中提取令牌。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class AuthTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * 私有构造函数，禁止实例化。
     */
    private AuthTokenResolver() {
    }

    /**
     * 解析请求中的访问令牌，优先级：Authorization > X-Auth-Token。
     *
     * @param request HTTP 请求
     * @return 解析到的令牌字符串，未找到返回 null
     */
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
        return null;
    }
}
