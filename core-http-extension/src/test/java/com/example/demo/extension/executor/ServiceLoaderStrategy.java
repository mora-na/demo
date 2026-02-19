package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiExecutionContext;
import com.example.demo.extension.api.executor.ExecuteStrategy;

/**
 * ServiceLoader 测试策略。
 */
public class ServiceLoaderStrategy implements ExecuteStrategy {
    @Override
    public String type() {
        return "TEST";
    }

    @Override
    public int version() {
        return 10;
    }

    @Override
    public DynamicApiExecuteResult execute(DynamicApiExecutionContext context) {
        return DynamicApiExecuteResult.success("service");
    }
}
