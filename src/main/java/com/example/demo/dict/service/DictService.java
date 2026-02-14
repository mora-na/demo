package com.example.demo.dict.service;

import com.example.demo.dict.dto.DictDataVO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 字典读取与缓存服务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public interface DictService {

    /**
     * 获取指定字典类型的可用数据项。
     */
    List<DictDataVO> getDataByType(String dictType);

    /**
     * 批量获取字典类型数据。
     */
    Map<String, List<DictDataVO>> getDataByTypes(Collection<String> dictTypes);

    /**
     * 获取全部启用字典数据。
     */
    Map<String, List<DictDataVO>> getAllEnabled();

    /**
     * 获取字典标签。
     */
    String getLabel(String dictType, String value);

    /**
     * 清理并刷新缓存。
     */
    void refreshCache();

    /**
     * 清理指定字典类型缓存。
     */
    void refreshCache(String dictType);
}
