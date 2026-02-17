package com.example.demo.extension.adapter;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.web.limit.RateLimitProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Locale;

/**
 * 动态接口限流适配器，复用现有限流配置与缓存实现。
 */
@Component
public class RateLimitAdapter {

    private final RateLimitProperties properties;
    private final CacheTool cacheTool;
    private final CommonConstants commonConstants;
    private final ObjectProvider<RateLimitPolicyProvider> policyProvider;

    public RateLimitAdapter(RateLimitProperties properties,
                            CacheTool cacheTool,
                            CommonConstants commonConstants,
                            ObjectProvider<RateLimitPolicyProvider> policyProvider) {
        this.properties = properties;
        this.cacheTool = cacheTool;
        this.commonConstants = commonConstants;
        this.policyProvider = policyProvider;
    }

    public RateLimitDecision tryAcquire(HttpServletRequest request, String policyId) {
        if (properties == null || !properties.isEnabled()) {
            return RateLimitDecision.allow();
        }
        RateLimitPolicy policy = resolvePolicy(policyId);
        long windowSeconds = properties.getWindowSeconds();
        int maxRequests = properties.getMaxRequests();
        String keyMode = properties.getKeyMode();
        boolean includePath = properties.isIncludePath();
        if (policy != null) {
            if (policy.getWindowSeconds() > 0) {
                windowSeconds = policy.getWindowSeconds();
            }
            if (policy.getMaxRequests() > 0) {
                maxRequests = policy.getMaxRequests();
            }
            if (StringUtils.isNotBlank(policy.getKeyMode())) {
                keyMode = policy.getKeyMode();
            }
            includePath = policy.isIncludePath();
        }
        long windowMillis = Math.max(1, windowSeconds) * 1000L;
        int max = Math.max(1, maxRequests);
        String key = buildKey(request, keyMode, includePath);
        if (key == null) {
            return RateLimitDecision.allow();
        }
        Duration ttl = Duration.ofMillis(windowMillis);
        Boolean created = cacheTool.setIfAbsent(key, "1", ttl);
        if (Boolean.TRUE.equals(created)) {
            return RateLimitDecision.allow();
        }
        Long count = cacheTool.increment(key);
        if (count == null || count <= max) {
            return RateLimitDecision.allow();
        }
        return RateLimitDecision.reject(commonConstants.getRateLimit().getMessageKey());
    }

    private RateLimitPolicy resolvePolicy(String policyId) {
        if (StringUtils.isBlank(policyId)) {
            return null;
        }
        RateLimitPolicyProvider provider = policyProvider == null ? null : policyProvider.getIfAvailable();
        if (provider == null) {
            return null;
        }
        return provider.resolve(policyId);
    }

    private String buildKey(HttpServletRequest request, String keyMode, boolean includePath) {
        if (request == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(commonConstants.getRateLimit().getKeyPrefix());
        if (includePath) {
            builder.append(request.getRequestURI());
        }
        builder.append(':');
        String identity = resolveIdentity(request, keyMode);
        builder.append(identity);
        return builder.toString();
    }

    private String resolveIdentity(HttpServletRequest request, String mode) {
        String normalized = mode == null ? "" : mode.toLowerCase(Locale.ROOT);
        AuthUser user = AuthContext.get();
        String userId = user == null ? null : String.valueOf(user.getId());
        String ip = resolveClientIp(request);
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

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(commonConstants.getHttp().getForwardedForHeader());
        if (StringUtils.isNotBlank(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader(commonConstants.getHttp().getRealIpHeader());
        if (StringUtils.isNotBlank(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
