package com.example.demo.extension.api.executor;

/**
 * 动态接口任务终止原因。
 */
public enum DynamicApiTerminationReason {
    TIMEOUT,
    ERROR,
    CANCELLED,
    REJECTED,
    CIRCUIT_OPEN
}
