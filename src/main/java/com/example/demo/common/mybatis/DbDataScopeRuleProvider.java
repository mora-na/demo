package com.example.demo.common.mybatis;

import com.example.demo.datascope.service.DataScopeRuleService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "security.data-scope", name = "source", havingValue = "db", matchIfMissing = true)
public class DbDataScopeRuleProvider implements DataScopeRuleProvider {

    private final ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider;
    private final DataScopeProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DATA_SCOPE_RULE_KEY = "data-scope:rules";

    public DbDataScopeRuleProvider(ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider,
                                   DataScopeProperties properties,
                                   RedisTemplate<String, Object> redisTemplate) {
        this.dataScopeRuleServiceProvider = dataScopeRuleServiceProvider;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Map<String, String> getTableColumnMap() {
        long ttlSeconds = properties.getCacheSeconds();
        if (ttlSeconds <= 0) {
            return safeMap(fetchRules());
        }
        Map<String, String> cached = readCache();
        if (cached != null) {
            return cached;
        }
        Map<String, String> fresh = safeMap(fetchRules());
        redisTemplate.opsForValue().set(DATA_SCOPE_RULE_KEY, fresh, Duration.ofSeconds(ttlSeconds));
        return fresh;
    }

    private Map<String, String> fetchRules() {
        DataScopeRuleService service = dataScopeRuleServiceProvider.getIfAvailable();
        if (service == null) {
            return Collections.emptyMap();
        }
        return service.getEnabledTableColumnMap();
    }

    private Map<String, String> safeMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(map);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readCache() {
        Object value = redisTemplate.opsForValue().get(DATA_SCOPE_RULE_KEY);
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            return new HashMap<>((Map<String, String>) value);
        }
        return null;
    }
}
