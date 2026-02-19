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
    private final Iterable<String> allowedClassPrefixes;

    public CacheSerializer(ObjectMapper objectMapper, Iterable<String> allowedClassPrefixes) {
        this.objectMapper = objectMapper;
        this.allowedClassPrefixes = allowedClassPrefixes;
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
        if (!isAllowedClass(className)) {
            throw new IllegalStateException("Cache value class is not allowed: " + className);
        }
        try {
            Class<?> clazz = resolveClass(className);
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

    private boolean isAllowedClass(String className) {
        if (allowedClassPrefixes == null) {
            return true;
        }
        boolean hasRule = false;
        for (String prefix : allowedClassPrefixes) {
            if (prefix == null || prefix.trim().isEmpty()) {
                continue;
            }
            hasRule = true;
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return !hasRule;
    }

    private Class<?> resolveClass(String className) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return Class.forName(className);
    }
}
