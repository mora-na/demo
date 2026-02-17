package com.example.demo.extension.registry;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;

import java.util.*;

/**
 * 注册表快照，提供不可变读视图。
 */
final class RegistrySnapshot {

    static final RegistrySnapshot EMPTY = new RegistrySnapshot(Collections.emptyMap(), Collections.emptyMap());

    private final Map<String, DynamicApiMeta> exactMappings;
    private final Map<String, List<DynamicApiMeta>> patternMappings;

    RegistrySnapshot(Map<String, DynamicApiMeta> exactMappings,
                     Map<String, List<DynamicApiMeta>> patternMappings) {
        this.exactMappings = exactMappings;
        this.patternMappings = patternMappings;
    }

    static RegistrySnapshot from(Collection<DynamicApiMeta> metas) {
        if (metas == null || metas.isEmpty()) {
            return EMPTY;
        }
        Map<String, DynamicApiMeta> exact = new HashMap<>();
        Map<String, List<DynamicApiMeta>> patterns = new HashMap<>();
        for (DynamicApiMeta meta : metas) {
            String method = DynamicApiRegistry.normalizeMethod(meta.getApi().getMethod());
            if (DynamicApiRegistry.isPatternPath(meta.getApi().getPath())) {
                patterns.computeIfAbsent(method, key -> new ArrayList<>()).add(meta);
            } else {
                exact.put(DynamicApiRegistry.buildKey(method, meta.getApi().getPath()), meta);
            }
        }
        Map<String, List<DynamicApiMeta>> patternSnapshot = new HashMap<>();
        for (Map.Entry<String, List<DynamicApiMeta>> entry : patterns.entrySet()) {
            List<DynamicApiMeta> list = new ArrayList<>(entry.getValue());
            list.sort(Comparator.comparing(DynamicApiMeta::getPathPattern, PathPattern.SPECIFICITY_COMPARATOR));
            patternSnapshot.put(entry.getKey(), Collections.unmodifiableList(list));
        }
        return new RegistrySnapshot(Collections.unmodifiableMap(exact), Collections.unmodifiableMap(patternSnapshot));
    }

    DynamicApiMatch match(String method, String path) {
        String key = DynamicApiRegistry.buildKey(method, path);
        DynamicApiMeta exact = exactMappings.get(key);
        if (exact != null) {
            return new DynamicApiMatch(exact, Collections.emptyMap());
        }
        List<DynamicApiMeta> patterns = patternMappings.get(method);
        if (patterns == null || patterns.isEmpty()) {
            return null;
        }
        PathContainer container = PathContainer.parsePath(path);
        for (DynamicApiMeta meta : patterns) {
            PathPattern.PathMatchInfo info = meta.getPathPattern().matchAndExtract(container);
            if (info != null) {
                return new DynamicApiMatch(meta, info.getUriVariables());
            }
        }
        return null;
    }

    RegistrySnapshot with(DynamicApiMeta meta, boolean isPattern) {
        Map<String, DynamicApiMeta> exactCopy = new HashMap<>(exactMappings);
        Map<String, List<DynamicApiMeta>> patternCopy = new HashMap<>(patternMappings);
        String method = DynamicApiRegistry.normalizeMethod(meta.getApi().getMethod());
        if (isPattern) {
            List<DynamicApiMeta> list = new ArrayList<>(patternCopy.getOrDefault(method, Collections.emptyList()));
            list.removeIf(item -> sameKey(item, meta.getApi().getMethod(), meta.getApi().getPath()));
            list.add(meta);
            list.sort(Comparator.comparing(DynamicApiMeta::getPathPattern, PathPattern.SPECIFICITY_COMPARATOR));
            patternCopy.put(method, Collections.unmodifiableList(list));
        } else {
            exactCopy.put(DynamicApiRegistry.buildKey(method, meta.getApi().getPath()), meta);
        }
        return new RegistrySnapshot(Collections.unmodifiableMap(exactCopy), Collections.unmodifiableMap(patternCopy));
    }

    RegistrySnapshot without(String method, String path) {
        Map<String, DynamicApiMeta> exactCopy = new HashMap<>(exactMappings);
        Map<String, List<DynamicApiMeta>> patternCopy = new HashMap<>(patternMappings);
        String normalized = DynamicApiRegistry.normalizeMethod(method);
        exactCopy.remove(DynamicApiRegistry.buildKey(normalized, path));
        List<DynamicApiMeta> list = patternCopy.get(normalized);
        if (list != null && !list.isEmpty()) {
            List<DynamicApiMeta> updated = new ArrayList<>(list);
            updated.removeIf(item -> sameKey(item, method, path));
            if (updated.isEmpty()) {
                patternCopy.remove(normalized);
            } else {
                updated.sort(Comparator.comparing(DynamicApiMeta::getPathPattern, PathPattern.SPECIFICITY_COMPARATOR));
                patternCopy.put(normalized, Collections.unmodifiableList(updated));
            }
        }
        return new RegistrySnapshot(Collections.unmodifiableMap(exactCopy), Collections.unmodifiableMap(patternCopy));
    }

    private boolean sameKey(DynamicApiMeta meta, String method, String path) {
        if (meta == null || meta.getApi() == null) {
            return false;
        }
        String metaMethod = DynamicApiRegistry.normalizeMethod(meta.getApi().getMethod());
        String metaPath = meta.getApi().getPath();
        return metaMethod.equals(DynamicApiRegistry.normalizeMethod(method))
                && Objects.equals(metaPath, path);
    }
}
