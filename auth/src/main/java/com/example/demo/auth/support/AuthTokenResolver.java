package com.example.demo.auth.support;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 认证令牌解析器，按优先级从请求头或参数中提取令牌。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class AuthTokenResolver {

    private final AuthConstants systemConstants;
    private final AuthProperties authProperties;

    public AuthTokenResolver(AuthConstants systemConstants,
                             AuthProperties authProperties) {
        this.systemConstants = systemConstants;
        this.authProperties = authProperties;
    }

    /**
     * 解析请求中的访问令牌，优先级：Authorization > X-Auth-Token > Cookie > Query。
     *
     * @param request HTTP 请求
     * @return 解析到的令牌字符串，未找到返回 null
     */
    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        AuthConstants.Token tokenConstants = systemConstants.getToken();
        String authorization = StringUtils.trimToNull(request.getHeader(tokenConstants.getAuthorizationHeader()));
        if (StringUtils.isNotBlank(authorization)) {
            String bearerPrefix = tokenConstants.getBearerPrefix();
            if (StringUtils.isNotBlank(bearerPrefix)
                    && authorization.regionMatches(true, 0, bearerPrefix, 0, bearerPrefix.length())) {
                String token = authorization.substring(bearerPrefix.length()).trim();
                return StringUtils.isNotBlank(token) ? token : null;
            }
            if (!authProperties.getJwt().isStrictBearer()) {
                return authorization.trim();
            }
        }
        String token = StringUtils.trimToNull(request.getHeader(tokenConstants.getFallbackTokenHeader()));
        if (StringUtils.isNotBlank(token)) {
            return token.trim();
        }
        String cookieToken = resolveCookieToken(request);
        if (StringUtils.isNotBlank(cookieToken)) {
            return cookieToken;
        }
        if (authProperties.getJwt().isAllowQueryToken()) {
            String queryToken = request.getParameter(tokenConstants.getQueryTokenParameter());
            if (StringUtils.isNotBlank(queryToken)) {
                return queryToken;
            }
        }
        return null;
    }

    private String resolveCookieToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        AuthProperties.Jwt.Cookie cookieConfig = authProperties.getJwt().getCookie();
        if (cookieConfig == null || !cookieConfig.isEnabled()) {
            return null;
        }
        String cookieName = StringUtils.trimToNull(cookieConfig.getName());
        if (StringUtils.isBlank(cookieName)) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && cookieName.equals(cookie.getName())) {
                String token = StringUtils.trimToNull(cookie.getValue());
                if (StringUtils.isNotBlank(token)) {
                    return token;
                }
            }
        }
        return null;
    }
}
