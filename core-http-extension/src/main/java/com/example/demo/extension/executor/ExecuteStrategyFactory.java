package com.example.demo.extension.executor;

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

    public ExecuteStrategyFactory(List<ExecuteStrategy> strategies) {
        List<DynamicApiTypeMeta> metas = new ArrayList<>();
        if (strategies != null) {
            for (ExecuteStrategy strategy : strategies) {
                if (strategy == null) {
                    continue;
                }
                String type = normalizeType(strategy.type());
                if (StringUtils.isBlank(type)) {
                    continue;
                }
                ExecuteStrategy existing = strategyMap.put(type, strategy);
                if (existing != null) {
                    log.warn("Duplicate dynamic api type registered: type={}, old={}, new={}",
                            type, existing.getClass().getName(), strategy.getClass().getName());
                }
                DynamicApiTypeMeta meta = new DynamicApiTypeMeta();
                meta.setCode(type);
                String name = StringUtils.trimToNull(strategy.displayName());
                meta.setName(name == null ? type : name);
                metas.add(meta);
            }
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
}
