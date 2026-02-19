package com.example.demo.extension.executor;

import com.example.demo.extension.api.executor.ExecuteStrategy;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.dto.DynamicApiTypeMeta;
import com.example.demo.extension.model.DynamicApiTypeCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 执行策略工厂。
 */
@Slf4j
@Component
public class ExecuteStrategyFactory {

    private final Map<String, ExecuteStrategy> strategyMap = new HashMap<>();
    private final List<DynamicApiTypeMeta> typeMetas;

    public ExecuteStrategyFactory(List<ExecuteStrategy> strategies,
                                  DynamicApiProperties properties) {
        List<DynamicApiTypeMeta> metas = new ArrayList<>();
        List<ExecuteStrategy> combined = new ArrayList<>();
        if (strategies != null) {
            combined.addAll(strategies);
        }
        if (properties != null && properties.getStrategy() != null
                && properties.getStrategy().isEnableServiceLoader()) {
            ServiceLoader<ExecuteStrategy> loader = ServiceLoader.load(ExecuteStrategy.class,
                    ExecuteStrategy.class.getClassLoader());
            for (ExecuteStrategy strategy : loader) {
                combined.add(strategy);
            }
        }
        String duplicatePolicy = properties == null || properties.getStrategy() == null
                ? "REPLACE" : properties.getStrategy().getDuplicateTypePolicy();
        for (ExecuteStrategy strategy : combined) {
            if (strategy == null) {
                continue;
            }
            String type = normalizeType(strategy.type());
            if (StringUtils.isBlank(type)) {
                continue;
            }
            ExecuteStrategy existing = strategyMap.get(type);
            ExecuteStrategy selected = resolveDuplicate(type, existing, strategy, duplicatePolicy);
            if (selected == null) {
                continue;
            }
            strategyMap.put(type, selected);
        }
        for (Map.Entry<String, ExecuteStrategy> entry : strategyMap.entrySet()) {
            String type = entry.getKey();
            ExecuteStrategy strategy = entry.getValue();
            DynamicApiTypeMeta meta = new DynamicApiTypeMeta();
            meta.setCode(type);
            String name = StringUtils.trimToNull(strategy.displayName());
            meta.setName(name == null ? type : name);
            metas.add(meta);
        }
        metas.sort((left, right) -> String.CASE_INSENSITIVE_ORDER.compare(left.getCode(), right.getCode()));
        this.typeMetas = Collections.unmodifiableList(metas);
    }

    public ExecuteStrategy get(String type) {
        return type == null ? null : strategyMap.get(normalizeType(type));
    }

    public List<DynamicApiTypeMeta> listTypes() {
        return typeMetas;
    }

    public String normalizeType(String type) {
        return DynamicApiTypeCodes.normalize(type);
    }

    private ExecuteStrategy resolveDuplicate(String type,
                                             ExecuteStrategy existing,
                                             ExecuteStrategy incoming,
                                             String policy) {
        if (existing == null) {
            return incoming;
        }
        String normalized = policy == null ? "REPLACE" : policy.trim().toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "KEEP_FIRST":
                log.warn("Duplicate dynamic api type ignored: type={}, existing={}, incoming={}",
                        type, existing.getClass().getName(), incoming.getClass().getName());
                return existing;
            case "FAIL":
                throw new IllegalStateException("Duplicate dynamic api type: " + type
                        + " existing=" + existing.getClass().getName()
                        + " incoming=" + incoming.getClass().getName());
            case "PREFER_HIGHEST_VERSION":
                if (incoming.version() > existing.version()) {
                    log.warn("Duplicate dynamic api type replaced by higher version: type={}, old={}, new={}",
                            type, existing.getClass().getName(), incoming.getClass().getName());
                    return incoming;
                }
                log.warn("Duplicate dynamic api type kept (higher version wins): type={}, old={}, new={}",
                        type, existing.getClass().getName(), incoming.getClass().getName());
                return existing;
            case "REPLACE":
            default:
                log.warn("Duplicate dynamic api type replaced: type={}, old={}, new={}",
                        type, existing.getClass().getName(), incoming.getClass().getName());
                return incoming;
        }
    }
}
