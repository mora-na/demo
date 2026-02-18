package com.example.demo.common.web.permission;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限拦截器，基于注解与配置进行登录与权限校验。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PermissionProperties properties;
    private final PermissionService permissionService;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final I18nService i18nService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final CommonConstants systemConstants;
    private final ObjectProvider<PermissionBypassEvaluator> bypassEvaluators;

    /**
     * 构造函数，注入权限配置、服务与公共排除路径。
     *
     * @param properties         权限配置
     * @param permissionService  权限服务
     * @param commonExcludePaths 公共排除路径配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public PermissionInterceptor(PermissionProperties properties,
                                 PermissionService permissionService,
                                 CommonExcludePathsProperties commonExcludePaths,
                                 I18nService i18nService,
                                 CommonConstants systemConstants,
                                 ObjectProvider<PermissionBypassEvaluator> bypassEvaluators) {
        this.properties = properties;
        this.permissionService = permissionService;
        this.commonExcludePaths = commonExcludePaths;
        this.i18nService = i18nService;
        this.systemConstants = systemConstants;
        this.bypassEvaluators = bypassEvaluators;
    }

    /**
     * 拦截请求并执行登录与权限校验。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  处理器
     * @return true 表示放行
     * @throws Exception 校验异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }
        if (!properties.isEnabled()) {
            return true;
        }
        if (shouldBypassByEvaluator(request)) {
            return true;
        }
        if (isExcluded(request)) {
            return true;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission permission = resolvePermission(handlerMethod);
        RequireLogin login = resolveLogin(handlerMethod);
        boolean loginRequired = login != null || permission != null || properties.isRequireLoginByDefault();
        AuthUser user = AuthContext.get();
        if (loginRequired && user == null) {
            writeUnauthorized(response, i18nService.getMessage(request, systemConstants.getPermission().getRequiredMessageKey()));
            return false;
        }
        if (permission == null) {
            return true;
        }
        List<String> required = Arrays.stream(permission.value())
                .filter(value -> value != null && !value.trim().isEmpty())
                .collect(Collectors.toList());
        if (required.isEmpty()) {
            return true;
        }
        boolean allowed;
        if (permission.logical() == Logical.OR) {
            allowed = permissionService.hasAnyPermission(user, required);
        } else {
            allowed = permissionService.hasAllPermissions(user, required);
        }
        if (!allowed) {
            writeForbidden(response, i18nService.getMessage(request, systemConstants.getPermission().getDeniedMessageKey()));
            return false;
        }
        return true;
    }

    /**
     * 判断当前请求是否在权限排除路径中。
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

    /**
     * 解析方法或类上的 RequirePermission 注解。
     *
     * @param handlerMethod 处理器方法
     * @return RequirePermission 注解或 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private RequirePermission resolvePermission(HandlerMethod handlerMethod) {
        RequirePermission permission = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequirePermission.class);
        if (permission != null) {
            return permission;
        }
        return AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), RequirePermission.class);
    }

    /**
     * 解析方法或类上的 RequireLogin 注解。
     *
     * @param handlerMethod 处理器方法
     * @return RequireLogin 注解或 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private RequireLogin resolveLogin(HandlerMethod handlerMethod) {
        RequireLogin login = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireLogin.class);
        if (login != null) {
            return login;
        }
        return AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), RequireLogin.class);
    }

    private boolean shouldBypassByEvaluator(HttpServletRequest request) {
        if (bypassEvaluators == null) {
            return false;
        }
        for (PermissionBypassEvaluator evaluator : bypassEvaluators) {
            try {
                if (evaluator != null && evaluator.shouldBypass(request)) {
                    return true;
                }
            } catch (Exception ignored) {
                // ignore evaluator errors to avoid blocking permission
            }
        }
        return false;
    }

    /**
     * 写出 401 未授权响应。
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws Exception IO 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_UNAUTHORIZED, message);
        writeJson(response, result);
    }

    /**
     * 写出 403 禁止访问响应。
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws Exception IO 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_FORBIDDEN, message);
        writeJson(response, result);
    }

    private void writeJson(HttpServletResponse response, CommonResult<Object> result) throws Exception {
        if (response.isCommitted()) {
            return;
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(systemConstants.getHttp().getJsonContentType());
        String body = OBJECT_MAPPER.writeValueAsString(result);
        try {
            response.getWriter().write(body);
        } catch (IllegalStateException ex) {
            try {
                response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
            } catch (IOException ignore) {
            }
        }
    }
}
