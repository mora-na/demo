package com.example.demo.common.mybatis;

import com.example.demo.common.cache.CacheTool;
import com.example.demo.datascope.entity.DataScopeRule;
import com.example.demo.datascope.service.DataScopeRuleService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于数据库的数据范围规则提供者，支持缓存。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnProperty(prefix = "security.data-scope", name = "source", havingValue = "db", matchIfMissing = true)
@Transactional(propagation = Propagation.NOT_SUPPORTED, readOnly = true)
public class DbDataScopeRuleProvider implements DataScopeRuleProvider {

    private static final String DATA_SCOPE_RULE_KEY = "data-scope:rules";
    private final ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider;
    private final DataScopeProperties properties;
    private final ObjectProvider<CacheTool> cacheToolProvider;

    /**
     * 构造函数，注入规则服务提供者与缓存配置。
     *
     * @param dataScopeRuleServiceProvider 规则服务提供者
     * @param properties                   数据范围配置
     * @param cacheToolProvider            缓存工具提供者
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DbDataScopeRuleProvider(ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider,
                                   DataScopeProperties properties,
                                   ObjectProvider<CacheTool> cacheToolProvider) {
        this.dataScopeRuleServiceProvider = dataScopeRuleServiceProvider;
        this.properties = properties;
        this.cacheToolProvider = cacheToolProvider;
    }

    /**
     * 获取表列映射，优先读取缓存。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public Map<String, DataScopeRuleDefinition> getRuleMap() {
        long ttlSeconds = properties.getCacheSeconds();
        if (ttlSeconds <= 0) {
            return safeMap(fetchRules());
        }
        CacheTool cacheTool = cacheToolProvider.getIfAvailable();
        Map<String, DataScopeRuleDefinition> cached = readCache(cacheTool);
        if (cached != null) {
            return cached;
        }
        Map<String, DataScopeRuleDefinition> fresh = safeMap(fetchRules());
        if (cacheTool != null) {
            cacheTool.set(DATA_SCOPE_RULE_KEY, fresh, Duration.ofSeconds(ttlSeconds));
        }
        return fresh;
    }

    /**
     * 从数据源拉取启用的规则映射。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Map<String, DataScopeRuleDefinition> fetchRules() {
        DataScopeRuleService service = dataScopeRuleServiceProvider.getIfAvailable();
        if (service == null) {
            return Collections.emptyMap();
        }
        Map<String, DataScopeRule> rules = service.getEnabledRules();
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, DataScopeRuleDefinition> result = new HashMap<>();
        for (Map.Entry<String, DataScopeRule> entry : rules.entrySet()) {
            String scopeKey = entry.getKey();
            DataScopeRule rule = entry.getValue();
            if (rule == null) {
                continue;
            }
            DataScopeRuleDefinition definition = new DataScopeRuleDefinition();
            definition.setScopeKey(scopeKey);
            definition.setTableName(rule.getTableName());
            definition.setTableAlias(rule.getTableAlias());
            definition.setDeptColumn(rule.getDeptColumn());
            definition.setUserColumn(rule.getUserColumn());
            definition.setFilterType(rule.getFilterType());
            definition.setStatus(rule.getStatus());
            result.put(scopeKey, definition);
        }
        return result;
    }

    /**
     * 对映射进行空安全处理与拷贝。
     *
     * @param map 原始映射
     * @return 安全映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Map<String, DataScopeRuleDefinition> safeMap(Map<String, DataScopeRuleDefinition> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(map);
    }

    /**
     * 读取缓存中的映射。
     *
     * @return 表列映射，未命中返回 null
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @SuppressWarnings("unchecked")
    private Map<String, DataScopeRuleDefinition> readCache(CacheTool cacheTool) {
        if (cacheTool == null) {
            return null;
        }
        Object value = cacheTool.get(DATA_SCOPE_RULE_KEY);
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            return new HashMap<>((Map<String, DataScopeRuleDefinition>) value);
        }
        return null;
    }
}
