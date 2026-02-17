package com.example.demo.extension.executor;

import com.example.demo.extension.model.DynamicApiType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 执行策略工厂。
 */
@Component
public class ExecuteStrategyFactory {

    private final Map<DynamicApiType, ExecuteStrategy> strategyMap = new EnumMap<>(DynamicApiType.class);

    public ExecuteStrategyFactory(List<ExecuteStrategy> strategies) {
        if (strategies != null) {
            for (ExecuteStrategy strategy : strategies) {
                if (strategy != null && strategy.type() != null) {
                    strategyMap.put(strategy.type(), strategy);
                }
            }
        }
    }

    public ExecuteStrategy get(DynamicApiType type) {
        return type == null ? null : strategyMap.get(type);
    }
}
