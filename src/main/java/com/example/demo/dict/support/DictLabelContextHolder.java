package com.example.demo.dict.support;

import com.example.demo.dict.dto.DictDataVO;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 字典标签请求级上下文，用于序列化阶段复用批量预取结果。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
public final class DictLabelContextHolder {

    private static final ThreadLocal<Map<String, Map<String, String>>> LABEL_CONTEXT = new ThreadLocal<>();

    private DictLabelContextHolder() {
    }

    public static void preload(Map<String, List<DictDataVO>> groupedDictData) {
        if (groupedDictData == null || groupedDictData.isEmpty()) {
            return;
        }
        Map<String, Map<String, String>> context = LABEL_CONTEXT.get();
        if (context == null) {
            context = new HashMap<>();
            LABEL_CONTEXT.set(context);
        }
        for (Map.Entry<String, List<DictDataVO>> entry : groupedDictData.entrySet()) {
            String dictType = StringUtils.trimToNull(entry.getKey());
            if (dictType == null) {
                continue;
            }
            Map<String, String> labels = context.computeIfAbsent(dictType, key -> new HashMap<>());
            List<DictDataVO> items = entry.getValue();
            if (items == null || items.isEmpty()) {
                continue;
            }
            for (DictDataVO item : items) {
                if (item == null || item.getDictValue() == null) {
                    continue;
                }
                labels.put(String.valueOf(item.getDictValue()), item.getDictLabel());
            }
        }
    }

    public static Map<String, String> getLabelsByType(String dictType) {
        String normalizedType = StringUtils.trimToNull(dictType);
        if (normalizedType == null) {
            return null;
        }
        Map<String, Map<String, String>> context = LABEL_CONTEXT.get();
        if (context == null || context.isEmpty()) {
            return null;
        }
        return context.get(normalizedType);
    }

    public static void clear() {
        LABEL_CONTEXT.remove();
    }
}
