package com.example.demo.extension.executor;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 动态接口执行策略。
 */
public interface ExecuteStrategy {

    String type();

    default String displayName() {
        return type();
    }

    default Object parseConfig(String configJson, ObjectMapper objectMapper) throws Exception {
        return configJson;
    }

    DynamicApiExecuteResult execute(DynamicApiContext context) throws Exception;
}
