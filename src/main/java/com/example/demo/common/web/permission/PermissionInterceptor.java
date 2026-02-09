package com.example.demo.common.web.permission;

import com.alibaba.fastjson2.JSON;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    private final PermissionProperties properties;
    private final PermissionService permissionService;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PermissionInterceptor(PermissionProperties properties,
                                 PermissionService permissionService,
                                 CommonExcludePathsProperties commonExcludePaths) {
        this.properties = properties;
        this.permissionService = permissionService;
        this.commonExcludePaths = commonExcludePaths;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!properties.isEnabled()) {
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
            writeUnauthorized(response, "authentication required");
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
            writeForbidden(response, "permission denied");
            return false;
        }
        return true;
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

    private RequirePermission resolvePermission(HandlerMethod handlerMethod) {
        RequirePermission permission = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequirePermission.class);
        if (permission != null) {
            return permission;
        }
        return AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), RequirePermission.class);
    }

    private RequireLogin resolveLogin(HandlerMethod handlerMethod) {
        RequireLogin login = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequireLogin.class);
        if (login != null) {
            return login;
        }
        return AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getBeanType(), RequireLogin.class);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_UNAUTHORIZED, message);
        response.getWriter().write(JSON.toJSONString(result));
    }

    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_FORBIDDEN, message);
        response.getWriter().write(JSON.toJSONString(result));
    }
}
