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

/**
 * XSS 请求体增强器，对 @RequestBody 的对象进行递归转义。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@ControllerAdvice
public class XssRequestBodyAdvice extends RequestBodyAdviceAdapter {

    private final XssProperties properties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 构造函数，注入 XSS 配置与公共排除路径。
     *
     * @param properties          XSS 配置
     * @param commonExcludePaths  公共排除路径配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public XssRequestBodyAdvice(XssProperties properties,
                                CommonExcludePathsProperties commonExcludePaths) {
        this.properties = properties;
        this.commonExcludePaths = commonExcludePaths;
    }

    /**
     * 指定是否启用请求体增强，当前实现对所有请求体生效。
     *
     * @param methodParameter 方法参数
     * @param targetType      目标类型
     * @param converterType   转换器类型
     * @return true 表示启用
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * 读取请求体后进行 XSS 过滤处理。
     *
     * @param body          反序列化后的请求体
     * @param inputMessage  输入消息
     * @param parameter     方法参数
     * @param targetType    目标类型
     * @param converterType 转换器类型
     * @return 处理后的请求体
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 判断当前请求是否在 XSS 排除路径中。
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
