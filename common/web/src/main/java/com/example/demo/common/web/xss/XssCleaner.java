package com.example.demo.common.web.xss;

import org.springframework.web.util.HtmlUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.*;

/**
 * XSS 清洗工具，提供字符串与对象图的递归转义能力。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class XssCleaner {

    /**
     * 私有构造函数，禁止实例化。
     */
    private XssCleaner() {
    }

    /**
     * 对字符串进行 HTML 转义。
     *
     * @param value 输入字符串
     * @return 转义后的字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(value);
    }

    /**
     * 对对象进行递归清洗，默认使用身份映射表避免循环引用。
     *
     * @param target 目标对象
     * @return 清洗后的对象（原对象被原地修改）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static Object sanitizeObject(Object target) {
        return sanitizeObject(target, new IdentityHashMap<>());
    }

    /**
     * 对对象进行递归清洗，使用访问标记避免循环引用。
     *
     * @param target  目标对象
     * @param visited 已访问对象映射
     * @return 清洗后的对象（原对象被原地修改）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 清洗 Map 的值。
     *
     * @param map     Map 实例
     * @param visited 已访问对象映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static void sanitizeMap(Map<?, ?> map, IdentityHashMap<Object, Boolean> visited) {
        @SuppressWarnings("unchecked")
        Map<Object, Object> mutable = (Map<Object, Object>) map;
        for (Map.Entry<Object, Object> entry : mutable.entrySet()) {
            Object value = entry.getValue();
            Object sanitized = sanitizeObject(value, visited);
            if (value != sanitized) {
                entry.setValue(sanitized);
            }
        }
    }

    /**
     * 清洗 List 的元素。
     *
     * @param list    List 实例
     * @param visited 已访问对象映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static void sanitizeList(List<?> list, IdentityHashMap<Object, Boolean> visited) {
        @SuppressWarnings("unchecked")
        List<Object> mutable = (List<Object>) list;
        for (int i = 0; i < mutable.size(); i++) {
            Object value = mutable.get(i);
            Object sanitized = sanitizeObject(value, visited);
            if (value != sanitized) {
                mutable.set(i, sanitized);
            }
        }
    }

    /**
     * 清洗 Set 的元素并保持去重语义。
     *
     * @param set     Set 实例
     * @param visited 已访问对象映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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
            @SuppressWarnings("unchecked")
            Set<Object> mutable = (Set<Object>) set;
            mutable.addAll(sanitizedSet);
        }
    }

    /**
     * 清洗通用集合的元素。
     *
     * @param collection 集合实例
     * @param visited    已访问对象映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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
            @SuppressWarnings("unchecked")
            Collection<Object> mutable = (Collection<Object>) collection;
            mutable.addAll(sanitizedList);
        }
    }

    /**
     * 清洗数组元素。
     *
     * @param array   数组对象
     * @param visited 已访问对象映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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

    /**
     * 清洗对象字段，递归遍历父类字段并跳过静态/终态字段。
     *
     * @param target  目标对象
     * @param type    当前类型
     * @param visited 已访问对象映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static void sanitizeFields(Object target, Class<?> type, IdentityHashMap<Object, Boolean> visited) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            Field[] fields = current.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers) || field.isSynthetic()) {
                    continue;
                }
                setAccessibleQuietly(field);
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

    /**
     * 判断类型是否为简单值类型，避免继续递归。
     *
     * @param type 类型
     * @return true 表示简单值类型
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
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
