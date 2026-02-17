package com.example.demo.extension.adapter;

import lombok.Getter;

/**
 * 限流判定结果。
 */
@Getter
public class RateLimitDecision {

    private final boolean allowed;
    private final String messageKey;

    public RateLimitDecision(boolean allowed, String messageKey) {
        this.allowed = allowed;
        this.messageKey = messageKey;
    }

    public static RateLimitDecision allow() {
        return new RateLimitDecision(true, null);
    }

    public static RateLimitDecision reject(String messageKey) {
        return new RateLimitDecision(false, messageKey);
    }
}
