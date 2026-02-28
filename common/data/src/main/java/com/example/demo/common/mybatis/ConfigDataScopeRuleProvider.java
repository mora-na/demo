package com.example.demo.common.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于配置的数据范围规则提供者。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnProperty(prefix = "security.data-scope", name = "source", havingValue = "config")
public class ConfigDataScopeRuleProvider implements DataScopeRuleProvider {

    private final DataScopeProperties properties;

    /**
     * 构造函数，注入数据范围配置。
     *
     * @param properties 数据范围配置
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public ConfigDataScopeRuleProvider(DataScopeProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取配置的表列映射并进行规范化。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public Map<String, DataScopeRuleDefinition> getRuleMap() {
        Map<String, String> map = properties.getTableColumnMap();
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DataScopeRuleDefinition> normalized = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String scopeKey = entry.getKey().trim();
            DataScopeRuleDefinition rule = new DataScopeRuleDefinition();
            rule.setScopeKey(scopeKey);
            rule.setTableName(scopeKey);
            rule.setDeptColumn(entry.getValue().trim());
            rule.setUserColumn("create_by");
            normalized.put(scopeKey, rule);
        }
        return normalized;
    }
}
