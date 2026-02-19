package com.example.demo.auth.support;

import com.example.demo.auth.config.AuthProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;

/**
 * JWT Cookie 读写支持。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/19
 */
@Component
public class AuthCookieService {

    private static final String SAME_SITE_LAX = "Lax";
    private static final String SAME_SITE_STRICT = "Strict";
    private static final String SAME_SITE_NONE = "None";

    private final AuthProperties authProperties;

    public AuthCookieService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    public boolean isEnabled() {
        return authProperties != null
                && authProperties.getJwt() != null
                && authProperties.getJwt().getCookie() != null
                && authProperties.getJwt().getCookie().isEnabled();
    }

    public void writeTokenCookie(HttpServletResponse response, String token) {
        if (!isEnabled() || response == null || StringUtils.isBlank(token)) {
            return;
        }
        AuthProperties.Jwt.Cookie cookie = authProperties.getJwt().getCookie();
        String name = normalizeName(cookie.getName());
        if (StringUtils.isBlank(name)) {
            return;
        }
        Long maxAgeSeconds = resolveMaxAgeSeconds(cookie);
        ResponseCookie responseCookie = buildCookie(name, token, cookie, maxAgeSeconds);
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    public void clearTokenCookie(HttpServletResponse response) {
        if (!isEnabled() || response == null) {
            return;
        }
        AuthProperties.Jwt.Cookie cookie = authProperties.getJwt().getCookie();
        String name = normalizeName(cookie.getName());
        if (StringUtils.isBlank(name)) {
            return;
        }
        ResponseCookie responseCookie = buildCookie(name, "", cookie, 0L);
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
    }

    private ResponseCookie buildCookie(String name,
                                       String value,
                                       AuthProperties.Jwt.Cookie cookie,
                                       Long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value == null ? "" : value);
        String path = StringUtils.trimToNull(cookie.getPath());
        builder.path(path == null ? "/" : path);
        String domain = StringUtils.trimToNull(cookie.getDomain());
        if (domain != null) {
            builder.domain(domain);
        }
        builder.httpOnly(cookie.isHttpOnly());
        builder.secure(cookie.isSecure());
        builder.sameSite(normalizeSameSite(cookie.getSameSite()));
        if (maxAgeSeconds != null) {
            builder.maxAge(Duration.ofSeconds(Math.max(0L, maxAgeSeconds)));
        }
        return builder.build();
    }

    private Long resolveMaxAgeSeconds(AuthProperties.Jwt.Cookie cookie) {
        long configured = cookie.getMaxAgeSeconds();
        if (configured > 0) {
            return configured;
        }
        if (configured == 0) {
            return null;
        }
        long ttl = authProperties.getJwt().getTtlSeconds();
        return ttl > 0 ? ttl : null;
    }

    private String normalizeSameSite(String value) {
        if (StringUtils.isBlank(value)) {
            return SAME_SITE_LAX;
        }
        if (SAME_SITE_STRICT.equalsIgnoreCase(value)) {
            return SAME_SITE_STRICT;
        }
        if (SAME_SITE_NONE.equalsIgnoreCase(value)) {
            return SAME_SITE_NONE;
        }
        return SAME_SITE_LAX;
    }

    private String normalizeName(String name) {
        return StringUtils.trimToNull(name);
    }
}
