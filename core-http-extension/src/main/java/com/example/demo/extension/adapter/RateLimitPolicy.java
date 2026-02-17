package com.example.demo.extension.adapter;

import lombok.Data;

/**
 * 动态接口限流策略。
 */
@Data
public class RateLimitPolicy {

    private long windowSeconds;
    private int maxRequests;
    private String keyMode;
    private boolean includePath;
}
