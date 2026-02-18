package com.example.demo.extension.adapter;

import com.example.demo.extension.config.DynamicApiProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于配置的限流策略提供器。
 */
@Component
public class ConfigRateLimitPolicyProvider implements RateLimitPolicyProvider {

    private final DynamicApiProperties properties;

    public ConfigRateLimitPolicyProvider(DynamicApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public RateLimitPolicy resolve(String policyId) {
        if (properties == null || StringUtils.isBlank(policyId)) {
            return null;
        }
        List<DynamicApiProperties.RateLimitPolicy> policies = properties.getRateLimitPolicies();
        if (policies == null || policies.isEmpty()) {
            return null;
        }
        for (DynamicApiProperties.RateLimitPolicy policy : policies) {
            if (policy == null || StringUtils.isBlank(policy.getId())) {
                continue;
            }
            if (!policy.getId().equalsIgnoreCase(policyId.trim())) {
                continue;
            }
            RateLimitPolicy resolved = new RateLimitPolicy();
            resolved.setWindowSeconds(policy.getWindowSeconds());
            resolved.setMaxRequests(policy.getMaxRequests());
            resolved.setKeyMode(policy.getKeyMode());
            resolved.setIncludePath(policy.isIncludePath());
            return resolved;
        }
        return null;
    }
}
