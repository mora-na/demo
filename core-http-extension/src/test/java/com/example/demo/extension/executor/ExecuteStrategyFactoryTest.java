package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.config.DynamicApiProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecuteStrategyFactoryTest {

    @Test
    void serviceLoaderPrefersHigherVersionWhenConfigured() {
        DynamicApiProperties properties = new DynamicApiProperties();
        properties.getStrategy().setEnableServiceLoader(true);
        properties.getStrategy().setDuplicateTypePolicy("PREFER_HIGHEST_VERSION");
        ExecuteStrategyFactory factory = new ExecuteStrategyFactory(
                Collections.singletonList(new LowVersionStrategy()), properties);
        ExecuteStrategy selected = factory.get("TEST");
        assertTrue(selected instanceof ServiceLoaderStrategy);
    }

    @Test
    void duplicatePolicyKeepsFirst() {
        DynamicApiProperties properties = new DynamicApiProperties();
        properties.getStrategy().setDuplicateTypePolicy("KEEP_FIRST");
        ExecuteStrategyFactory factory = new ExecuteStrategyFactory(
                Arrays.asList(new LowVersionStrategy(), new HighVersionStrategy()), properties);
        ExecuteStrategy selected = factory.get("TEST");
        assertTrue(selected instanceof LowVersionStrategy);
    }

    private static class LowVersionStrategy implements ExecuteStrategy {
        @Override
        public String type() {
            return "TEST";
        }

        @Override
        public int version() {
            return 1;
        }

        @Override
        public DynamicApiExecuteResult execute(com.example.demo.extension.api.executor.DynamicApiExecutionContext context) {
            return DynamicApiExecuteResult.success("low");
        }
    }

    private static class HighVersionStrategy implements ExecuteStrategy {
        @Override
        public String type() {
            return "TEST";
        }

        @Override
        public int version() {
            return 5;
        }

        @Override
        public DynamicApiExecuteResult execute(com.example.demo.extension.api.executor.DynamicApiExecutionContext context) {
            return DynamicApiExecuteResult.success("high");
        }
    }
}
