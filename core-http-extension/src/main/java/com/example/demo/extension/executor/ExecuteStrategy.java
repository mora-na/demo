package com.example.demo.extension.executor;

import com.example.demo.extension.model.DynamicApiType;

/**
 * 动态接口执行策略。
 */
public interface ExecuteStrategy {

    DynamicApiType type();

    DynamicApiExecuteResult execute(DynamicApiContext context) throws Exception;
}
