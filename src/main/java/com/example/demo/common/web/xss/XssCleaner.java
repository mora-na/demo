package com.example.demo.common.web.xss;

import org.springframework.web.util.HtmlUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.*;

public final class XssCleaner {

    private XssCleaner() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(value);
    }

    public static Object sanitizeObject(Object target) {
        return sanitizeObject(target, new IdentityHashMap<>());
    }

    private static Object sanitizeObject(Object target, IdentityHashMap<Object, Boolean> visited) {
        if (target == null) {
            return null;
        }
        if (target instanceof String) {
            return sanitize((String) target);
        }
        Class<?> type = target.getClass();
        if (isSimpleValueType(type)) {
            return target;
        }
        if (visited.containsKey(target)) {
            return target;
        }
        visited.put(target, Boolean.TRUE);

        if (target instanceof Map) {
            sanitizeMap((Map<?, ?>) target, visited);
            return target;
        }
        if (target instanceof List) {
            sanitizeList((List<?>) target, visited);
            return target;
        }
        if (target instanceof Set) {
            sanitizeSet((Set<?>) target, visited);
            return target;
        }
        if (target instanceof Collection) {
            sanitizeCollection((Collection<?>) target, visited);
            return target;
        }
        if (type.isArray()) {
            sanitizeArray(target, visited);
            return target;
        }

        sanitizeFields(target, type, visited);
        return target;
    }

    private static void sanitizeMap(Map<?, ?> map, IdentityHashMap<Object, Boolean> visited) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object sanitized = sanitizeObject(value, visited);
            if (value != sanitized) {
                ((Map.Entry<Object, Object>) entry).setValue(sanitized);
            }
        }
    }

    private static void sanitizeList(List<?> list, IdentityHashMap<Object, Boolean> visited) {
        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            Object sanitized = sanitizeObject(value, visited);
            if (value != sanitized) {
                ((List<Object>) list).set(i, sanitized);
            }
        }
    }

    private static void sanitizeSet(Set<?> set, IdentityHashMap<Object, Boolean> visited) {
        Set<Object> sanitizedSet = new LinkedHashSet<>(set.size());
        boolean changed = false;
        for (Object value : set) {
            Object sanitized = sanitizeObject(value, visited);
            sanitizedSet.add(sanitized);
            if (value != sanitized) {
                changed = true;
            }
        }
        if (changed) {
            set.clear();
            ((Set<Object>) set).addAll(sanitizedSet);
        }
    }

    private static void sanitizeCollection(Collection<?> collection, IdentityHashMap<Object, Boolean> visited) {
        List<Object> sanitizedList = new ArrayList<>(collection.size());
        boolean changed = false;
        for (Object value : collection) {
            Object sanitized = sanitizeObject(value, visited);
            sanitizedList.add(sanitized);
            if (value != sanitized) {
                changed = true;
            }
        }
        if (changed) {
            collection.clear();
            ((Collection<Object>) collection).addAll(sanitizedList);
        }
    }

    private static void sanitizeArray(Object array, IdentityHashMap<Object, Boolean> visited) {
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Object value = Array.get(array, i);
            Object sanitized = sanitizeObject(value, visited);
            if (value != sanitized) {
                Array.set(array, i, sanitized);
            }
        }
    }

    private static void sanitizeFields(Object target, Class<?> type, IdentityHashMap<Object, Boolean> visited) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || field.isSynthetic()) {
                    continue;
                }
                field.setAccessible(true);
                Object value;
                try {
                    value = field.get(target);
                } catch (IllegalAccessException e) {
                    continue;
                }
                Object sanitized = sanitizeObject(value, visited);
                if (value != sanitized) {
                    try {
                        field.set(target, sanitized);
                    } catch (IllegalAccessException ignored) {
                        // ignore fields we cannot set
                    }
                }
            }
            current = current.getSuperclass();
        }
    }

    private static boolean isSimpleValueType(Class<?> type) {
        if (type.isPrimitive() || type.isEnum()) {
            return true;
        }
        if (Number.class.isAssignableFrom(type)
                || CharSequence.class.isAssignableFrom(type)
                || Boolean.class == type
                || Character.class == type
                || Date.class.isAssignableFrom(type)
                || UUID.class == type
                || Temporal.class.isAssignableFrom(type)) {
            return true;
        }
        String name = type.getName();
        return name.startsWith("java.time.") || name.startsWith("java.sql.");
    }
}
