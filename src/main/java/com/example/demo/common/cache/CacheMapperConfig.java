package com.example.demo.common.cache;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库缓存 Mapper 条件注册。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Configuration
@ConditionalOnProperty(prefix = "cache", name = "location", havingValue = "db")
@MapperScan("com.example.demo.common.cache.mapper")
public class CacheMapperConfig {
}
