package com.example.demo.dict.web;

import com.example.demo.dict.annotation.DictLabel;
import com.example.demo.dict.config.DictConstants;
import com.example.demo.dict.support.DictTool;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

/**
 * 在响应序列化前批量预取字典类型，避免 DictLabel 逐条触发翻译查询。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@ControllerAdvice
public class DictLabelPreloadAdvice implements ResponseBodyAdvice<Object> {

    private static final int MAX_SCAN_DEPTH = 8;
    private static final int MAX_SCAN_NODES = 10000;

    private final DictConstants dictConstants;
    private final LruCache<Class<?>, Set<String>> classDictTypeCache;
    private final LruCache<Class<?>, List<Field>> classFieldCache;

    public DictLabelPreloadAdvice(DictConstants dictConstants) {
        this.dictConstants = dictConstants;
        int maxSize = resolveClassCacheMaxSize();
        this.classDictTypeCache = new LruCache<>(maxSize);
        this.classFieldCache = new LruCache<>(maxSize);
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return converterType != null && MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        if (body == null) {
            return null;
        }
        Set<String> dictTypes = collectDictTypes(body);
        if (!dictTypes.isEmpty()) {
            DictTool.preloadLabels(dictTypes);
        }
        return body;
    }

    private Set<String> collectDictTypes(Object body) {
        Set<String> dictTypes = new LinkedHashSet<>();
        IdentityHashMap<Object, Boolean> visited = new IdentityHashMap<>();
        int[] counter = new int[]{0};
        scanObject(body, dictTypes, visited, 0, counter);
        return dictTypes;
    }

    private void scanObject(Object value,
                            Set<String> dictTypes,
                            IdentityHashMap<Object, Boolean> visited,
                            int depth,
                            int[] counter) {
        if (value == null || depth > MAX_SCAN_DEPTH || counter[0] >= MAX_SCAN_NODES) {
            return;
        }
        Class<?> clazz = value.getClass();
        if (isSimpleValueType(clazz)) {
            return;
        }
        if (visited.put(value, Boolean.TRUE) != null) {
            return;
        }
        counter[0]++;

        if (value instanceof Collection) {
            for (Object item : (Collection<?>) value) {
                scanObject(item, dictTypes, visited, depth + 1, counter);
            }
            return;
        }
        if (value instanceof Map) {
            for (Object item : ((Map<?, ?>) value).values()) {
                scanObject(item, dictTypes, visited, depth + 1, counter);
            }
            return;
        }
        if (clazz.isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                scanObject(Array.get(value, i), dictTypes, visited, depth + 1, counter);
            }
            return;
        }
        if (isJdkClass(clazz)) {
            return;
        }

        dictTypes.addAll(resolveClassDictTypes(clazz));
        for (Field field : resolveClassFields(clazz)) {
            Object nested;
            try {
                nested = field.get(value);
            } catch (IllegalAccessException ignore) {
                continue;
            }
            scanObject(nested, dictTypes, visited, depth + 1, counter);
        }
    }

    private Set<String> resolveClassDictTypes(Class<?> clazz) {
        return classDictTypeCache.getOrCompute(clazz, () -> {
            Set<String> types = new LinkedHashSet<>();
            for (Field field : resolveClassFields(clazz)) {
                DictLabel annotation = field.getAnnotation(DictLabel.class);
                if (annotation == null) {
                    continue;
                }
                String dictType = StringUtils.trimToNull(annotation.value());
                if (dictType != null) {
                    types.add(dictType);
                }
            }
            return types;
        });
    }

    private static void setAccessibleQuietly(Field field) {
        if (field == null) {
            return;
        }
        try {
            field.setAccessible(true);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private List<Field> resolveClassFields(Class<?> clazz) {
        return classFieldCache.getOrCompute(clazz, () -> {
            List<Field> fields = new ArrayList<>();
            Class<?> cursor = clazz;
            while (cursor != null && cursor != Object.class && !isJdkClass(cursor)) {
                for (Field field : cursor.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    setAccessibleQuietly(field);
                    fields.add(field);
                }
                cursor = cursor.getSuperclass();
            }
            return fields;
        });
    }

    private boolean isSimpleValueType(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        return clazz.isPrimitive()
                || Number.class.isAssignableFrom(clazz)
                || CharSequence.class.isAssignableFrom(clazz)
                || Boolean.class == clazz
                || Character.class == clazz
                || Date.class.isAssignableFrom(clazz)
                || Enum.class.isAssignableFrom(clazz)
                || clazz.getName().startsWith("java.time.");
    }

    private boolean isJdkClass(Class<?> clazz) {
        if (clazz == null) {
            return true;
        }
        String name = clazz.getName();
        return name.startsWith("java.")
                || name.startsWith("javax.")
                || name.startsWith("sun.")
                || name.startsWith("jdk.");
    }

    private int resolveClassCacheMaxSize() {
        if (dictConstants == null || dictConstants.getPreload() == null) {
            return DictConstants.Preload.DEFAULT_CLASS_CACHE_MAX_SIZE;
        }
        int configured = dictConstants.getPreload().getClassCacheMaxSize();
        return configured <= 0 ? DictConstants.Preload.DEFAULT_CLASS_CACHE_MAX_SIZE : configured;
    }

    private static final class LruCache<K, V> {
        private final int maxSize;
        private final LinkedHashMap<K, V> store;

        private LruCache(int maxSize) {
            this.maxSize = Math.max(1, maxSize);
            this.store = new LinkedHashMap<K, V>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > LruCache.this.maxSize;
                }
            };
        }

        private V getOrCompute(K key, Supplier<V> supplier) {
            V cached;
            synchronized (store) {
                cached = store.get(key);
            }
            if (cached != null) {
                return cached;
            }
            V computed = supplier.get();
            synchronized (store) {
                V second = store.get(key);
                if (second != null) {
                    return second;
                }
                store.put(key, computed);
                return computed;
            }
        }
    }
}
