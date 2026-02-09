package com.example.demo.common.web.filter;

import com.alibaba.fastjson2.JSON;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.limit.DuplicateSubmitProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DuplicateSubmitFilter extends OncePerRequestFilter {

    private final DuplicateSubmitProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final StringRedisTemplate stringRedisTemplate;

    public DuplicateSubmitFilter(DuplicateSubmitProperties properties,
                                 CommonExcludePathsProperties commonExcludePaths,
                                 StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        if (!isMatchMethod(request.getMethod())) {
            return true;
        }
        List<String> excludePaths = commonExcludePaths.merge(properties.getExcludePaths());
        if (excludePaths == null || excludePaths.isEmpty()) {
            return false;
        }
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        for (String pattern : excludePaths) {
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
        long interval = Math.max(1, properties.getIntervalMillis());
        if (isMultipart(request) && !hasIdempotencyKey(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        CachedBodyHttpServletRequest wrapped = isMultipart(request) ? null : new CachedBodyHttpServletRequest(request);
        HttpServletRequest keyRequest = wrapped == null ? request : wrapped;
        String bodyHash = wrapped == null ? null : DigestUtils.md5DigestAsHex(wrapped.getCachedBody());
        String key = buildKey(keyRequest, bodyHash);
        if (isDuplicate(key, interval)) {
            writeDuplicate(response);
            return;
        }
        filterChain.doFilter(wrapped == null ? request : wrapped, response);
    }

    private boolean isDuplicate(String key, long interval) {
        if (key == null) {
            return false;
        }
        Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMillis(interval));
        return Boolean.FALSE.equals(created);
    }

    private String buildKey(HttpServletRequest request, String bodyHash) {
        StringBuilder builder = new StringBuilder("dup:");
        if (properties.isIncludePath()) {
            builder.append(request.getRequestURI());
        }
        builder.append(':');
        String identity = resolveIdentity(request, properties.getKeyMode());
        builder.append(identity);
        String idempotency = resolveIdempotencyKey(request);
        if (idempotency != null) {
            builder.append(":k=").append(idempotency);
            return builder.toString();
        }
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            builder.append(":q=").append(query);
        }
        if (properties.isIncludeBody() && bodyHash != null) {
            builder.append(":b=").append(bodyHash);
        }
        return builder.toString();
    }

    private String resolveIdempotencyKey(HttpServletRequest request) {
        if (!properties.isUseIdempotencyKey()) {
            return null;
        }
        String headerName = properties.getHeaderName();
        if (headerName == null || headerName.trim().isEmpty()) {
            headerName = "Idempotency-Key";
        }
        String value = request.getHeader(headerName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String resolveIdentity(HttpServletRequest request, String mode) {
        String normalized = mode == null ? "" : mode.toLowerCase(Locale.ROOT);
        AuthUser user = AuthContext.get();
        String userId = user == null ? null : String.valueOf(user.getId());
        String ip = resolveClientIp(request);
        if ("user".equals(normalized)) {
            return userId == null ? ip : userId;
        }
        if ("ip-user".equals(normalized)) {
            if (userId == null) {
                return ip;
            }
            return ip + ":" + userId;
        }
        return ip;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.trim().isEmpty()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/");
    }

    private boolean hasIdempotencyKey(HttpServletRequest request) {
        return resolveIdempotencyKey(request) != null;
    }

    private boolean isMatchMethod(String method) {
        if (method == null) {
            return false;
        }
        List<String> methods = properties.getMethods();
        if (methods == null || methods.isEmpty()) {
            return false;
        }
        for (String configured : methods) {
            if (method.equalsIgnoreCase(configured)) {
                return true;
            }
        }
        return false;
    }

    private void writeDuplicate(HttpServletResponse response) throws IOException {
        response.setStatus(409);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(409, "duplicate submission detected");
        response.getWriter().write(JSON.toJSONString(result));
    }
}
