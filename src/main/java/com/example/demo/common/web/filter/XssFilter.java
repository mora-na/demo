package com.example.demo.common.web.filter;

import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.xss.XssHttpServletRequestWrapper;
import com.example.demo.common.web.xss.XssProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 * XSS 过滤器，对请求参数进行转义处理。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    private final XssProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final CommonConstants systemConstants;

    /**
     * 构造函数，注入 XSS 配置与公共排除路径。
     *
     * @param properties         XSS 配置
     * @param commonExcludePaths 公共排除路径配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public XssFilter(XssProperties properties,
                     CommonExcludePathsProperties commonExcludePaths,
                     CommonConstants systemConstants) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
        this.systemConstants = systemConstants;
    }

    /**
     * 对请求进行 XSS 过滤，必要时包装请求对象。
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!properties.isEnabled() || isExcluded(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        String prefix = systemConstants.getHttp().getMultipartPrefix();
        String contentType = httpRequest.getContentType();
        if (contentType != null && contentType.toLowerCase(java.util.Locale.ROOT).startsWith(prefix)) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
    }

    /**
     * 判断当前请求是否在排除路径中。
     *
     * @param request HTTP 请求
     * @return true 表示排除
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private boolean isExcluded(HttpServletRequest request) {
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
}
