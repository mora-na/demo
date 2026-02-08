package com.example.demo.common.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "security.data-scope", name = "source", havingValue = "config")
public class ConfigDataScopeRuleProvider implements DataScopeRuleProvider {

    private final DataScopeProperties properties;

    public ConfigDataScopeRuleProvider(DataScopeProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, String> getTableColumnMap() {
        Map<String, String> map = properties.getTableColumnMap();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> normalized = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            normalized.put(entry.getKey().trim().toLowerCase(), entry.getValue());
        }
        return normalized;
    }
}
