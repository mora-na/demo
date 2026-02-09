package com.example.demo.common.web.filter;

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

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class XssFilter implements Filter {

    private final XssProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public XssFilter(XssProperties properties,
                     CommonExcludePathsProperties commonExcludePaths) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
    }

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
        chain.doFilter(new XssHttpServletRequestWrapper(httpRequest), response);
    }

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
