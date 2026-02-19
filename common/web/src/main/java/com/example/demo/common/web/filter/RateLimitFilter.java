package com.example.demo.common.web.filter;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.limit.RateLimitProperties;
import com.example.demo.common.web.support.ClientIpResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
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
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * 限流过滤器，基于缓存计数器。
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
    private final CacheTool cacheTool;
    private final I18nService i18nService;
    private final CommonConstants systemConstants;
    private final ObjectMapper objectMapper;
    private final ClientIpResolver clientIpResolver;

    /**
     * 构造函数，注入限流配置与缓存工具。
     *
     * @param properties         限流配置
     * @param commonExcludePaths 公共排除路径配置
     * @param cacheTool          缓存工具
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public RateLimitFilter(RateLimitProperties properties,
                           CommonExcludePathsProperties commonExcludePaths,
                           CacheTool cacheTool,
                           I18nService i18nService,
                           CommonConstants systemConstants,
                           ObjectMapper objectMapper,
                           ClientIpResolver clientIpResolver) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
        this.cacheTool = cacheTool;
        this.i18nService = i18nService;
        this.systemConstants = systemConstants;
        this.objectMapper = objectMapper;
        this.clientIpResolver = clientIpResolver;
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
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
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
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long windowMillis = Math.max(1, properties.getWindowSeconds()) * 1000L;
        int maxRequests = Math.max(1, properties.getMaxRequests());
        String key = buildKey(request);
        boolean allowed = tryAcquire(key, windowMillis, maxRequests);
        if (!allowed) {
            writeRateLimited(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 尝试获取限流许可，使用计数器控制窗口内请求量。
     *
     * @param key          限流 Key
     * @param windowMillis 时间窗口（毫秒）
     * @param maxRequests  最大请求数
     * @return true 表示允许
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean tryAcquire(String key, long windowMillis, int maxRequests) {
        if (key == null) {
            return true;
        }
        Duration ttl = Duration.ofMillis(windowMillis);
        Boolean created = cacheTool.setIfAbsent(key, "1", ttl);
        if (Boolean.TRUE.equals(created)) {
            return true;
        }
        Long count = cacheTool.increment(key);
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
        StringBuilder builder = new StringBuilder(systemConstants.getRateLimit().getKeyPrefix());
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
        String ip = clientIpResolver.resolve(request);
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

    /**
     * 写出限流响应。
     *
     * @param response HTTP 响应
     * @throws IOException IO 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private void writeRateLimited(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int status = systemConstants.getRateLimit().getResponseStatus();
        response.setStatus(status);
        response.setContentType(systemConstants.getHttp().getJsonContentType());
        String message = i18nService.getMessage(request, systemConstants.getRateLimit().getMessageKey());
        CommonResult<Object> result = CommonResult.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
