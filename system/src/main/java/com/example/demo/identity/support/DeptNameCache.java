package com.example.demo.identity.support;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;

/**
 * 部门名称本地缓存，避免重复查询。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
@Component
public class DeptNameCache {

    private final Cache<Long, String> cache = Caffeine.newBuilder()
            .maximumSize(5000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    public String getIfPresent(Long deptId) {
        if (deptId == null) {
            return null;
        }
        return cache.getIfPresent(deptId);
    }

    public void put(Long deptId, String name) {
        if (deptId == null || name == null) {
            return;
        }
        cache.put(deptId, name);
    }

    public void invalidate(Long deptId) {
        if (deptId == null) {
            return;
        }
        cache.invalidate(deptId);
    }

    public void invalidateAll(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return;
        }
        cache.invalidateAll(deptIds);
    }
}
