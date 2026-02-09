package com.example.demo.common.web.filter;

import com.alibaba.fastjson2.JSON;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.limit.RateLimitProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final StringRedisTemplate stringRedisTemplate;

    public RateLimitFilter(RateLimitProperties properties,
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
        long windowMillis = Math.max(1, properties.getWindowSeconds()) * 1000L;
        int maxRequests = Math.max(1, properties.getMaxRequests());
        String key = buildKey(request);
        boolean allowed = tryAcquire(key, windowMillis, maxRequests);
        if (!allowed) {
            writeRateLimited(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean tryAcquire(String key, long windowMillis, int maxRequests) {
        if (key == null) {
            return true;
        }
        Duration ttl = Duration.ofMillis(windowMillis);
        Boolean created = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        if (Boolean.TRUE.equals(created)) {
            return true;
        }
        Long count = stringRedisTemplate.opsForValue().increment(key);
        return count == null || count <= maxRequests;
    }

    private String buildKey(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder("rl:");
        if (properties.isIncludePath()) {
            builder.append(request.getRequestURI());
        }
        builder.append(':');
        String identity = resolveIdentity(request, properties.getKeyMode());
        builder.append(identity);
        return builder.toString();
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

    private void writeRateLimited(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(429, "rate limit exceeded");
        response.getWriter().write(JSON.toJSONString(result));
    }
}
