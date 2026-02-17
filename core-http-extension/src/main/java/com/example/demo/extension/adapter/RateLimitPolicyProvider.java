package com.example.demo.extension.adapter;

/**
 * 限流策略提供器。
 */
public interface RateLimitPolicyProvider {

    RateLimitPolicy resolve(String policyId);
}
