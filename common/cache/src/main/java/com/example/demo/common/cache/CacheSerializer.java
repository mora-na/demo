package com.example.demo.common.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 缓存序列化器，用于数据库存储。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class CacheSerializer {

    private final ObjectMapper objectMapper;

    public CacheSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize cache value", ex);
        }
    }

    public Object deserialize(String json, String className) {
        if (json == null) {
            return null;
        }
        if (className == null || className.trim().isEmpty()) {
            return deserializeAsObject(json);
        }
        try {
            Class<?> clazz = Class.forName(className);
            return objectMapper.readValue(json, clazz);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize cache value", ex);
        }
    }

    private Object deserializeAsObject(String json) {
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize cache value", ex);
        }
    }
}
