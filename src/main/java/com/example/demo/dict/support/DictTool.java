package com.example.demo.dict.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.dict.dto.DictDataVO;
import com.example.demo.dict.service.DictService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 字典工具类，便于在业务层快速获取字典值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public final class DictTool {

    private DictTool() {
    }

    public static String getLabel(String dictType, Object value) {
        if (StringUtils.isBlank(dictType) || value == null) {
            return value == null ? null : String.valueOf(value);
        }
        DictService service = SpringContextHolder.getBean(DictService.class);
        if (service == null) {
            return String.valueOf(value);
        }
        return service.getLabel(dictType, String.valueOf(value));
    }

    public static List<DictDataVO> getDataByType(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyList();
        }
        DictService service = SpringContextHolder.getBean(DictService.class);
        if (service == null) {
            return Collections.emptyList();
        }
        return service.getDataByType(dictType);
    }

    public static Map<String, List<DictDataVO>> getAll() {
        DictService service = SpringContextHolder.getBean(DictService.class);
        if (service == null) {
            return Collections.emptyMap();
        }
        return service.getAllEnabled();
    }
}
