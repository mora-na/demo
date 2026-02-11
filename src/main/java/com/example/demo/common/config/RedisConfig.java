package com.example.demo.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * RedisTemplate 配置，统一序列化策略。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
@ConditionalOnProperty(prefix = "cache", name = "location", havingValue = "redis", matchIfMissing = true)
public class RedisConfig {

    /**
     * 构建 RedisTemplate，设置 Key 与 Value 的序列化器。
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate 实例
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }
}
