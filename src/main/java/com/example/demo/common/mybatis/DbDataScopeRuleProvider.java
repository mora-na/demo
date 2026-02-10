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

/**
 * 基于数据库的数据范围规则提供者，支持 Redis 缓存。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnProperty(prefix = "security.data-scope", name = "source", havingValue = "db", matchIfMissing = true)
public class DbDataScopeRuleProvider implements DataScopeRuleProvider {

    private static final String DATA_SCOPE_RULE_KEY = "data-scope:rules";
    private final ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider;
    private final DataScopeProperties properties;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 构造函数，注入规则服务提供者与缓存配置。
     *
     * @param dataScopeRuleServiceProvider 规则服务提供者
     * @param properties                   数据范围配置
     * @param redisTemplate                Redis 模板
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public DbDataScopeRuleProvider(ObjectProvider<DataScopeRuleService> dataScopeRuleServiceProvider,
                                   DataScopeProperties properties,
                                   RedisTemplate<String, Object> redisTemplate) {
        this.dataScopeRuleServiceProvider = dataScopeRuleServiceProvider;
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取表列映射，优先读取缓存。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 从数据源拉取启用的规则映射。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Map<String, String> fetchRules() {
        DataScopeRuleService service = dataScopeRuleServiceProvider.getIfAvailable();
        if (service == null) {
            return Collections.emptyMap();
        }
        return service.getEnabledTableColumnMap();
    }

    /**
     * 对映射进行空安全处理与拷贝。
     *
     * @param map 原始映射
     * @return 安全映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private Map<String, String> safeMap(Map<String, String> map) {
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
