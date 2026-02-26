package com.example.demo.auth.web;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.AuthUserStatusCache;
import com.example.demo.auth.service.PasswordPolicyService;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.support.AuthTokenResolver;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.permission.AuthBypassEvaluator;
import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import com.example.demo.identity.api.dto.IdentityDataScopeProfileDTO;
import com.example.demo.identity.api.dto.IdentityRoleDataScopeDTO;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.example.demo.identity.api.dto.IdentityUserScopeOverrideDTO;
import com.example.demo.identity.api.facade.IdentityReadFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.factory.ObjectProvider;
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
import java.util.*;

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

    private final ObjectMapper objectMapper;

    private final AuthProperties authProperties;
    private final CommonExcludePathsProperties commonExcludePaths;
    private final TokenService tokenService;
    private final PasswordPolicyService passwordPolicyService;
    private final IdentityReadFacade identityReadFacade;
    private final AuthUserStatusCache authUserStatusCache;
    private final I18nService i18nService;
    private final AuthConstants authConstants;
    private final CommonConstants commonConstants;
    private final AuthTokenResolver authTokenResolver;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectProvider<AuthBypassEvaluator> bypassEvaluators;

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
        if (authConstants.getFilter().getOptionsMethod().equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (shouldBypassByEvaluator(request)) {
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

    private boolean shouldBypassByEvaluator(HttpServletRequest request) {
        if (bypassEvaluators == null) {
            return false;
        }
        for (AuthBypassEvaluator evaluator : bypassEvaluators) {
            try {
                if (evaluator != null && evaluator.shouldBypass(request)) {
                    return true;
                }
            } catch (Exception ignored) {
                // ignore evaluator errors to avoid blocking auth
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
        String token = authTokenResolver.resolve(request);
        AuthConstants.Filter filterConstants = authConstants.getFilter();
        if (StringUtils.isBlank(token)) {
            writeUnauthorized(response, i18nService.getMessage(request, filterConstants.getTokenMissingMessageKey()));
            return;
        }
        AuthUser user = tokenService.verifyToken(token);
        if (user == null) {
            writeUnauthorized(response, i18nService.getMessage(request, filterConstants.getTokenInvalidMessageKey()));
            return;
        }
        if (user.getId() == null) {
            writeUnauthorized(response, i18nService.getMessage(request, filterConstants.getUserInvalidMessageKey()));
            return;
        }
        AuthUserStatusCache.Snapshot snapshot = authUserStatusCache.getSnapshot(user.getId());
        IdentityUserDTO dbUser = snapshot == null ? null : snapshot.getUser();
        IdentityDataScopeProfileDTO cachedProfile = snapshot == null ? null : snapshot.getProfile();
        boolean profileCacheMissKnown = snapshot != null
                && snapshot.isProfileCacheEnabled()
                && !snapshot.isProfilePresent();
        if (dbUser == null) {
            dbUser = identityReadFacade.getUserById(user.getId());
            int ttlSeconds = authProperties.getCache().getUserStatusTtlSeconds();
            if (dbUser != null && ttlSeconds > 0) {
                authUserStatusCache.put(user.getId(), dbUser, ttlSeconds);
            } else if (dbUser == null) {
                int negativeTtl = authProperties.getCache().getUserStatusNegativeTtlSeconds();
                authUserStatusCache.putNegative(user.getId(), negativeTtl);
            }
        } else if (passwordPolicyService.isPasswordChangeRequired(dbUser)) {
            IdentityUserDTO refreshed = identityReadFacade.getUserById(user.getId());
            if (refreshed != null) {
                dbUser = refreshed;
                int ttlSeconds = authProperties.getCache().getUserStatusTtlSeconds();
                if (ttlSeconds > 0) {
                    authUserStatusCache.put(user.getId(), refreshed, ttlSeconds);
                }
            }
        }
        if (dbUser == null) {
            writeUnauthorized(response, i18nService.getMessage(request, filterConstants.getUserNotFoundMessageKey()));
            return;
        }
        if (dbUser.getStatus() != null && dbUser.getStatus().equals(0)) {
            writeForbidden(response, i18nService.getMessage(request, filterConstants.getUserDisabledMessageKey()));
            return;
        }
        if (passwordPolicyService.isPasswordChangeRequired(dbUser)
                && !isPasswordChangeAllowedPath(request, filterConstants)) {
            writeForbidden(response, i18nService.getMessage(request, filterConstants.getPasswordChangeRequiredMessageKey()));
            return;
        }
        user.setUserName(dbUser.getUserName());
        user.setNickName(dbUser.getNickName());
        user.setDeptId(dbUser.getDeptId());
        user.setDataScopeType(dbUser.getDataScopeType());
        user.setDataScopeValue(dbUser.getDataScopeValue());
        IdentityDataScopeProfileDTO profile = cachedProfile;
        if (profile == null) {
            profile = loadDataScopeProfile(user.getId(), profileCacheMissKnown);
        }
        applyDataScopeProfile(user, profile);
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
        int status = authConstants.getController().getUnauthorizedCode();
        response.setStatus(status);
        response.setContentType(commonConstants.getHttp().getJsonContentType());
        CommonResult<Object> result = CommonResult.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 写出 403 禁止访问响应。
     *
     * @param response HTTP 响应
     * @param message  错误信息
     * @throws IOException IO 异常
     */
    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        int status = authConstants.getController().getForbiddenCode();
        response.setStatus(status);
        response.setContentType(commonConstants.getHttp().getJsonContentType());
        CommonResult<Object> result = CommonResult.error(status, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    private boolean isPasswordChangeAllowedPath(HttpServletRequest request, AuthConstants.Filter filterConstants) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        boolean profilePath = pathMatcher.match(filterConstants.getPasswordChangeProfilePath(), path);
        boolean logoutPath = pathMatcher.match(filterConstants.getPasswordChangeLogoutPath(), path);
        if (profilePath) {
            return filterConstants.getGetMethod().equalsIgnoreCase(method)
                    || filterConstants.getPutMethod().equalsIgnoreCase(method);
        }
        return logoutPath && filterConstants.getPostMethod().equalsIgnoreCase(method);
    }

    private IdentityDataScopeProfileDTO loadDataScopeProfile(Long userId, boolean cacheMissKnown) {
        if (userId == null) {
            return null;
        }
        if (!cacheMissKnown) {
            IdentityDataScopeProfileDTO cached = authUserStatusCache.getProfile(userId);
            if (cached != null) {
                return cached;
            }
        }
        IdentityDataScopeProfileDTO profile = identityReadFacade.buildDataScopeProfile(userId);
        int ttlSeconds = authProperties.getCache().getDataScopeProfileTtlSeconds();
        if (profile != null && ttlSeconds > 0) {
            authUserStatusCache.putProfile(userId, profile, ttlSeconds);
        }
        return profile;
    }

    private void applyDataScopeProfile(AuthUser user, IdentityDataScopeProfileDTO profile) {
        if (user == null) {
            return;
        }
        if (profile == null) {
            user.setDeptTreeIds(Collections.emptySet());
            user.setRoleDataScopes(Collections.emptyList());
            user.setUserScopeOverrides(Collections.emptyMap());
            return;
        }
        if (profile.getDeptTreeIds() == null || profile.getDeptTreeIds().isEmpty()) {
            user.setDeptTreeIds(Collections.emptySet());
        } else {
            user.setDeptTreeIds(new LinkedHashSet<>(profile.getDeptTreeIds()));
        }
        user.setRoleDataScopes(mapRoleDataScopes(profile.getRoleDataScopes()));
        user.setUserScopeOverrides(mapUserScopeOverrides(profile.getUserScopeOverrides()));
    }

    private java.util.List<RoleDataScope> mapRoleDataScopes(java.util.List<IdentityRoleDataScopeDTO> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }
        java.util.List<RoleDataScope> result = new ArrayList<>(sources.size());
        for (IdentityRoleDataScopeDTO source : sources) {
            if (source == null) {
                continue;
            }
            RoleDataScope scope = new RoleDataScope();
            scope.setRoleId(source.getRoleId());
            scope.setRoleCode(source.getRoleCode());
            scope.setDataScopeType(source.getDataScopeType());
            if (source.getCustomDeptIds() != null) {
                scope.setCustomDeptIds(new LinkedHashSet<>(source.getCustomDeptIds()));
            }
            if (source.getMenuDataScopes() != null) {
                scope.setMenuDataScopes(new LinkedHashMap<>(source.getMenuDataScopes()));
            }
            if (source.getMenuCustomDepts() != null) {
                LinkedHashMap<String, java.util.Set<Long>> menuCustoms = new LinkedHashMap<>();
                for (Map.Entry<String, java.util.Set<Long>> entry : source.getMenuCustomDepts().entrySet()) {
                    menuCustoms.put(entry.getKey(),
                            entry.getValue() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(entry.getValue()));
                }
                scope.setMenuCustomDepts(menuCustoms);
            }
            result.add(scope);
        }
        return result;
    }

    private Map<String, UserScopeOverride> mapUserScopeOverrides(Map<String, IdentityUserScopeOverrideDTO> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, UserScopeOverride> result = new LinkedHashMap<>();
        for (Map.Entry<String, IdentityUserScopeOverrideDTO> entry : sources.entrySet()) {
            IdentityUserScopeOverrideDTO source = entry.getValue();
            if (source == null) {
                continue;
            }
            UserScopeOverride override = new UserScopeOverride();
            override.setScopeKey(source.getScopeKey());
            override.setDataScopeType(source.getDataScopeType());
            if (source.getCustomDeptIds() != null) {
                override.setCustomDeptIds(new LinkedHashSet<>(source.getCustomDeptIds()));
            }
            override.setStatus(source.getStatus());
            result.put(entry.getKey(), override);
        }
        return result;
    }
}
