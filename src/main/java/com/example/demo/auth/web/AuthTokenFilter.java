package com.example.demo.auth.web;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.support.AuthTokenResolver;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.datascope.service.DataScopeResolver;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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

/**
 * 认证过滤器，校验请求令牌并注入认证上下文。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final AuthProperties authProperties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final TokenService tokenService;
    private final UserService userService;
    private final DataScopeResolver dataScopeResolver;
    private final I18nService i18nService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 判断当前请求是否跳过认证过滤。
     *
     * @param request HTTP 请求
     * @return true 表示跳过过滤
     */
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!authProperties.getFilter().isEnabled()) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        for (String pattern : commonExcludePaths.merge(authProperties.getFilter().getExcludePaths())) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行认证过滤逻辑，验证令牌、加载用户并写入上下文。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException      IO 异常
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = AuthTokenResolver.resolve(request);
        if (StringUtils.isBlank(token)) {
            writeUnauthorized(response, i18nService.getMessage(request, "auth.token.missing"));
            return;
        }
        AuthUser user = tokenService.verifyToken(token);
        if (user == null) {
            writeUnauthorized(response, i18nService.getMessage(request, "auth.token.invalid"));
            return;
        }
        if (user.getId() == null) {
            writeUnauthorized(response, i18nService.getMessage(request, "auth.user.invalid"));
            return;
        }
        User dbUser = userService.getById(user.getId());
        if (dbUser == null) {
            writeUnauthorized(response, i18nService.getMessage(request, "auth.user.not.found"));
            return;
        }
        if (dbUser.getStatus() != null && dbUser.getStatus().equals(User.STATUS_DISABLED)) {
            writeForbidden(response, i18nService.getMessage(request, "auth.user.disabled"));
            return;
        }
        user.setUserName(dbUser.getUserName());
        user.setNickName(dbUser.getNickName());
        user.setDeptId(dbUser.getDeptId());
        DataScopeResolver.DataScopeResult dataScope = dataScopeResolver.resolve(dbUser);
        user.setDataScopeType(dataScope.getType());
        user.setDataScopeValue(dataScope.getValue());
        AuthContext.set(user);
        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthContext.clear();
        }
    }

    /**
     * 写出 401 未授权响应。
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws IOException IO 异常
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_UNAUTHORIZED, message);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
    }

    /**
     * 写出 403 禁止访问响应。
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws IOException IO 异常
     */
    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        CommonResult<Object> result = CommonResult.error(HttpServletResponse.SC_FORBIDDEN, message);
        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(result));
    }
}
