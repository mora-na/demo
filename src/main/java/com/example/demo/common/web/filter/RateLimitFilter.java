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

/**
 * 限流过滤器，基于 Redis 计数实现窗口限流。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造函数，注入限流配置与 Redis 模板。
     *
     * @param properties          限流配置
     * @param commonExcludePaths  公共排除路径配置
     * @param stringRedisTemplate Redis 模板
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public RateLimitFilter(RateLimitProperties properties,
                           CommonExcludePathsProperties commonExcludePaths,
                           StringRedisTemplate stringRedisTemplate) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 判断当前请求是否跳过限流。
     *
     * @param request HTTP 请求
     * @return true 表示跳过
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 执行限流检查并在超限时返回错误响应。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 尝试获取限流许可，使用计数器控制窗口内请求量。
     *
     * @param key         限流 Key
     * @param windowMillis 时间窗口（毫秒）
     * @param maxRequests 最大请求数
     * @return true 表示允许
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 构建限流 Key。
     *
     * @param request HTTP 请求
     * @return 限流 Key
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 解析限流身份信息，支持 IP、用户或组合模式。
     *
     * @param request HTTP 请求
     * @param mode    Key 模式
     * @return 身份标识
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 获取客户端 IP，优先使用代理头。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 写出限流响应。
     *
     * @param response HTTP 响应
     * @throws IOException IO 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private void writeRateLimited(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(429, "rate limit exceeded");
        response.getWriter().write(JSON.toJSONString(result));
    }
}
