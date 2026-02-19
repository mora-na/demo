package com.example.demo.common.cache;

import com.example.demo.common.cache.mapper.CacheMapper;
import com.example.demo.common.config.CommonConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 缓存存储配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
public class CacheStoreConfig {

    @Bean
    @ConditionalOnProperty(prefix = "cache", name = "location", havingValue = "redis", matchIfMissing = true)
    public CacheStore redisCacheStore(RedisTemplate<String, Object> redisTemplate,
                                      StringRedisTemplate stringRedisTemplate) {
        return new RedisCacheStore(redisTemplate, stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(prefix = "cache", name = "location", havingValue = "memory")
    public CacheStore memoryCacheStore(CacheProperties cacheProperties, CommonConstants systemConstants) {
        return new MemoryCacheStore(cacheProperties == null ? null : cacheProperties.getMemory(), systemConstants);
    }

    @Bean
    @ConditionalOnProperty(prefix = "cache", name = "location", havingValue = "db")
    public CacheStore dbCacheStore(CacheMapper cacheMapper,
                                   ObjectMapper objectMapper,
                                   CacheProperties cacheProperties,
                                   CommonConstants systemConstants) {
        CacheProperties.Db db = cacheProperties == null ? null : cacheProperties.getDb();
        CacheSerializer serializer = new CacheSerializer(objectMapper,
                db == null ? null : db.getAllowedValueClassPrefixes());
        return new DbCacheStore(cacheMapper, serializer, db, systemConstants);
    }
}
