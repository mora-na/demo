package com.example.demo.extension.api.executor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 动态接口执行策略（SPI）。
 */
public interface ExecuteStrategy {

    String type();

    default String displayName() {
        return type();
    }

    default Object parseConfig(String configJson, ObjectMapper objectMapper) throws Exception {
        return configJson;
    }

    /**
     * 超时后的清理回调（可选）。
     */
    default void onTimeout(DynamicApiExecutionContext context) throws Exception {
        // default no-op
    }

    /**
     * 执行异常后的清理回调（可选）。
     */
    default void onError(DynamicApiExecutionContext context, Throwable error) throws Exception {
        // default no-op
    }

    /**
     * 任务取消后的清理回调（可选）。
     */
    default void onCancel(DynamicApiExecutionContext context, Throwable cause) throws Exception {
        // default no-op
    }

    /**
     * 协作式中断检查（可选调用）。
     */
    default void checkInterrupted() throws InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Dynamic api execution interrupted");
        }
    }

    DynamicApiExecuteResult execute(DynamicApiExecutionContext context) throws Exception;
}
