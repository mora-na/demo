package com.example.demo.extension.dto;

import lombok.Data;

/**
 * 动态接口限流策略描述。
 */
@Data
public class DynamicApiRateLimitPolicyMeta {

    private String id;

    private String name;

    private long windowSeconds;

    private int maxRequests;

    private String keyMode;

    private boolean includePath;
}
