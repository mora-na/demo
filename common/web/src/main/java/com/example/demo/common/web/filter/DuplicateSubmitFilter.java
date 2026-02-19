package com.example.demo.common.web.filter;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.limit.DuplicateSubmitProperties;
import com.example.demo.common.web.support.ClientIpResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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

/**
 * 重复提交过滤器，基于缓存工具。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DuplicateSubmitFilter extends OncePerRequestFilter {

    private final DuplicateSubmitProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final CacheTool cacheTool;
    private final I18nService i18nService;
    private final CommonConstants systemConstants;
    private final ObjectMapper objectMapper;
    private final ClientIpResolver clientIpResolver;

    /**
     * 构造函数，注入重复提交配置与缓存工具。
     *
     * @param properties         重复提交配置
     * @param commonExcludePaths 公共排除路径配置
     * @param cacheTool          缓存工具
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DuplicateSubmitFilter(DuplicateSubmitProperties properties,
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
     * 判断当前请求是否跳过重复提交过滤。
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

    /**
     * 执行重复提交检测，超限时返回冲突响应。
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
        long interval = Math.max(1, properties.getIntervalMillis());
        boolean multipart = isMultipart(request);
        if (multipart && !hasIdempotencyKey(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        CachedBodyHttpServletRequest wrapped = null;
        String bodyHash = null;
        if (properties.isIncludeBody() && !multipart) {
            long maxBodyBytes = Math.max(0, properties.getMaxBodyBytes());
            if (maxBodyBytes <= 0) {
                wrapped = new CachedBodyHttpServletRequest(request);
                bodyHash = DigestUtils.md5DigestAsHex(wrapped.getCachedBody());
            } else {
                int length = request.getContentLength();
                if (length >= 0 && length <= maxBodyBytes) {
                    wrapped = new CachedBodyHttpServletRequest(request, (int) maxBodyBytes);
                    if (!wrapped.isBodyTooLarge()) {
                        bodyHash = DigestUtils.md5DigestAsHex(wrapped.getCachedBody());
                    }
                }
            }
        }
        HttpServletRequest keyRequest = wrapped == null ? request : wrapped;
        String key = buildKey(keyRequest, bodyHash);
        if (isDuplicate(key, interval)) {
            writeDuplicate(request, response);
            return;
        }
        filterChain.doFilter(wrapped == null ? request : wrapped, response);
    }

    /**
     * 判断是否为重复提交。
     *
     * @param key      记录键
     * @param interval 判定间隔（毫秒）
     * @return true 表示重复
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isDuplicate(String key, long interval) {
        if (key == null) {
            return false;
        }
        Boolean created = cacheTool.setIfAbsent(key, "1", Duration.ofMillis(interval));
        return Boolean.FALSE.equals(created);
    }

    /**
     * 构建重复提交检测 Key。
     *
     * @param request  HTTP 请求
     * @param bodyHash 请求体摘要
     * @return Key 字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String buildKey(HttpServletRequest request, String bodyHash) {
        StringBuilder builder = new StringBuilder(systemConstants.getDuplicateSubmit().getKeyPrefix());
        if (properties.isIncludePath()) {
            builder.append(request.getRequestURI());
        }
        builder.append(':');
        String identity = resolveIdentity(request, properties.getKeyMode());
        builder.append(identity);
        String idempotency = resolveIdempotencyKey(request);
        if (idempotency != null) {
            builder.append(':')
                    .append(systemConstants.getDuplicateSubmit().getKeyIdempotencyTag())
                    .append('=')
                    .append(idempotency);
            return builder.toString();
        }
        String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            builder.append(':')
                    .append(systemConstants.getDuplicateSubmit().getKeyQueryTag())
                    .append('=')
                    .append(query);
        }
        if (properties.isIncludeBody() && bodyHash != null) {
            builder.append(':')
                    .append(systemConstants.getDuplicateSubmit().getKeyBodyTag())
                    .append('=')
                    .append(bodyHash);
        }
        return builder.toString();
    }

    /**
     * 解析幂等键 Header。
     *
     * @param request HTTP 请求
     * @return 幂等键值，未设置返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private String resolveIdempotencyKey(HttpServletRequest request) {
        if (!properties.isUseIdempotencyKey()) {
            return null;
        }
        String headerName = properties.getHeaderName();
        if (headerName == null || headerName.trim().isEmpty()) {
            headerName = systemConstants.getHttp().getIdempotencyHeaderDefault();
        }
        String value = request.getHeader(headerName);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 解析身份标识，支持 IP、用户或组合模式。
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
     * 判断请求是否为 multipart。
     *
     * @param request HTTP 请求
     * @return true 表示 multipart
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        String prefix = systemConstants.getHttp().getMultipartPrefix();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith(prefix);
    }

    /**
     * 判断请求是否携带幂等键。
     *
     * @param request HTTP 请求
     * @return true 表示存在幂等键
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean hasIdempotencyKey(HttpServletRequest request) {
        return resolveIdempotencyKey(request) != null;
    }

    /**
     * 判断当前方法是否在受保护的方法列表中。
     *
     * @param method HTTP 方法
     * @return true 表示匹配
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 写出重复提交响应。
     *
     * @param response HTTP 响应
     * @throws IOException IO 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private void writeDuplicate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int status = systemConstants.getDuplicateSubmit().getResponseStatus();
        response.setStatus(status);
        response.setContentType(systemConstants.getHttp().getJsonContentType());
        String message = i18nService.getMessage(request, systemConstants.getDuplicateSubmit().getMessageKey());
        CommonResult<Object> result = CommonResult.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
