package com.example.demo.common.web.xss;

import com.example.demo.common.web.CommonExcludePathsProperties;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.List;

@ControllerAdvice
public class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final XssProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public XssRequestBodyAdvice(XssProperties properties,
                                CommonExcludePathsProperties commonExcludePaths) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (!properties.isEnabled()) {
            return body;
        }
        if (inputMessage instanceof ServletServerHttpRequest) {
            HttpServletRequest request = ((ServletServerHttpRequest) inputMessage).getServletRequest();
            if (isExcluded(request)) {
                return body;
            }
        }
        return XssCleaner.sanitizeObject(body);
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
