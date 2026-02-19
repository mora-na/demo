package com.example.demo.dict.web;

import com.example.demo.dict.annotation.DictLabel;
import com.example.demo.dict.support.DictTool;
import org.apache.commons.lang3.StringUtils;
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
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<Class<?>, Set<String>> classDictTypeCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<Field>> classFieldCache = new ConcurrentHashMap<>();

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return converterType != null && MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
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
        return classDictTypeCache.computeIfAbsent(clazz, key -> {
            Set<String> types = new LinkedHashSet<>();
            for (Field field : resolveClassFields(key)) {
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
        return classFieldCache.computeIfAbsent(clazz, key -> {
            List<Field> fields = new ArrayList<>();
            Class<?> cursor = key;
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
}
