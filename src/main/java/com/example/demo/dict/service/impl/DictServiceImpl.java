package com.example.demo.dict.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.dict.config.DictConstants;
import com.example.demo.dict.dto.DictDataVO;
import com.example.demo.dict.entity.DictData;
import com.example.demo.dict.entity.DictType;
import com.example.demo.dict.service.DictDataService;
import com.example.demo.dict.service.DictService;
import com.example.demo.dict.service.DictTypeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 字典读取与缓存服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Service
public class DictServiceImpl implements DictService {

    private final DictDataService dictDataService;
    private final DictTypeService dictTypeService;
    private final CacheTool cacheTool;
    private final DictConstants dictConstants;
    private final ObjectMapper objectMapper;

    public DictServiceImpl(DictDataService dictDataService,
                           DictTypeService dictTypeService,
                           CacheTool cacheTool,
                           DictConstants dictConstants,
                           ObjectMapper objectMapper) {
        this.dictDataService = dictDataService;
        this.dictTypeService = dictTypeService;
        this.cacheTool = cacheTool;
        this.dictConstants = dictConstants;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<DictDataVO> getDataByType(String dictType) {
        if (StringUtils.isBlank(dictType)) {
            return Collections.emptyList();
        }
        String key = buildTypeKey(dictType);
        List<DictDataVO> cached = readListCache(key);
        if (cached != null) {
            return cached;
        }
        List<DictDataVO> fresh = queryEnabledDataByType(dictType);
        writeCache(key, fresh);
        return fresh;
    }

    @Override
    public Map<String, List<DictDataVO>> getDataByTypes(Collection<String> dictTypes) {
        if (dictTypes == null || dictTypes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> types = dictTypes.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (types.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<DictDataVO>> allCached = readMapCache(dictConstants.getCache().getAllKey());
        if (allCached != null && types.stream().allMatch(allCached::containsKey)) {
            return types.stream().collect(Collectors.toMap(type -> type, allCached::get, (a, b) -> a, LinkedHashMap::new));
        }
        Map<String, List<DictDataVO>> result = new LinkedHashMap<>();
        if (allCached != null) {
            for (String type : types) {
                if (allCached.containsKey(type)) {
                    result.put(type, allCached.get(type));
                }
            }
        }
        List<String> missing = types.stream().filter(type -> !result.containsKey(type)).collect(Collectors.toList());
        if (!missing.isEmpty()) {
            List<DictData> items = dictDataService.list(Wrappers.lambdaQuery(DictData.class)
                    .in(DictData::getDictType, missing)
                    .eq(DictData::getStatus, dictConstants.getStatus().getEnabled())
                    .orderByAsc(DictData::getSort)
                    .orderByAsc(DictData::getId));
            Map<String, List<DictDataVO>> grouped = toGroupMap(items);
            for (String type : missing) {
                List<DictDataVO> list = grouped.getOrDefault(type, Collections.emptyList());
                result.put(type, list);
                writeCache(buildTypeKey(type), list);
            }
        }
        return result;
    }

    @Override
    public Map<String, List<DictDataVO>> getAllEnabled() {
        Map<String, List<DictDataVO>> cached = readMapCache(dictConstants.getCache().getAllKey());
        if (cached != null) {
            return cached;
        }
        List<DictData> items = dictDataService.list(Wrappers.lambdaQuery(DictData.class)
                .eq(DictData::getStatus, dictConstants.getStatus().getEnabled())
                .orderByAsc(DictData::getSort)
                .orderByAsc(DictData::getId));
        Map<String, List<DictDataVO>> result = toGroupMap(items);
        writeCache(dictConstants.getCache().getAllKey(), result);
        return result;
    }

    @Override
    public String getLabel(String dictType, String value) {
        if (StringUtils.isBlank(dictType) || value == null) {
            return value;
        }
        List<DictDataVO> items = getDataByType(dictType);
        if (items.isEmpty()) {
            return value;
        }
        for (DictDataVO item : items) {
            if (value.equals(item.getDictValue())) {
                return item.getDictLabel();
            }
        }
        return value;
    }

    @Override
    public void refreshCache() {
        cacheTool.delete(dictConstants.getCache().getAllKey());
        List<DictType> types = dictTypeService.list();
        if (types != null) {
            for (DictType type : types) {
                if (type != null && StringUtils.isNotBlank(type.getDictType())) {
                    cacheTool.delete(buildTypeKey(type.getDictType()));
                }
            }
        }
    }

    @Override
    public void refreshCache(String dictType) {
        if (StringUtils.isNotBlank(dictType)) {
            cacheTool.delete(buildTypeKey(dictType));
        }
        cacheTool.delete(dictConstants.getCache().getAllKey());
    }

    private List<DictDataVO> queryEnabledDataByType(String dictType) {
        List<DictData> items = dictDataService.list(Wrappers.lambdaQuery(DictData.class)
                .eq(DictData::getDictType, dictType)
                .eq(DictData::getStatus, dictConstants.getStatus().getEnabled())
                .orderByAsc(DictData::getSort)
                .orderByAsc(DictData::getId));
        return toVOList(items);
    }

    private Map<String, List<DictDataVO>> toGroupMap(List<DictData> items) {
        if (items == null || items.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, List<DictDataVO>> grouped = new LinkedHashMap<>();
        for (DictData item : items) {
            if (item == null || StringUtils.isBlank(item.getDictType())) {
                continue;
            }
            grouped.computeIfAbsent(item.getDictType(), key -> new ArrayList<>())
                    .add(toVO(item));
        }
        return grouped;
    }

    private List<DictDataVO> toVOList(List<DictData> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream().map(this::toVO).collect(Collectors.toList());
    }

    private DictDataVO toVO(DictData item) {
        DictDataVO vo = new DictDataVO();
        vo.setId(item.getId());
        vo.setDictType(item.getDictType());
        vo.setDictLabel(item.getDictLabel());
        vo.setDictValue(item.getDictValue());
        vo.setStatus(item.getStatus());
        vo.setSort(item.getSort());
        vo.setRemark(item.getRemark());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private String buildTypeKey(String dictType) {
        return dictConstants.getCache().getKeyPrefix() + dictType;
    }

    private Duration resolveTtl() {
        long seconds = dictConstants.getCache().getTtlSeconds();
        if (seconds <= 0) {
            return null;
        }
        return Duration.ofSeconds(seconds);
    }

    private void writeCache(String key, Object value) {
        Duration ttl = resolveTtl();
        if (ttl == null) {
            return;
        }
        cacheTool.set(key, value, ttl);
    }

    @SuppressWarnings("unchecked")
    private List<DictDataVO> readListCache(String key) {
        Object value = cacheTool.get(key);
        if (!(value instanceof List)) {
            return null;
        }
        List<?> raw = (List<?>) value;
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        Object first = raw.get(0);
        if (first instanceof DictDataVO) {
            return (List<DictDataVO>) value;
        }
        return convertList(raw);
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<DictDataVO>> readMapCache(String key) {
        Object value = cacheTool.get(key);
        if (!(value instanceof Map)) {
            return null;
        }
        Map<?, ?> raw = (Map<?, ?>) value;
        if (raw.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Object firstValue = raw.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
        if (firstValue instanceof List) {
            List<?> list = (List<?>) firstValue;
            if (list.isEmpty() || list.get(0) instanceof DictDataVO) {
                return (Map<String, List<DictDataVO>>) value;
            }
        }
        return convertMap(raw);
    }

    private List<DictDataVO> convertList(List<?> raw) {
        List<DictDataVO> result = new ArrayList<>();
        for (Object item : raw) {
            if (item == null) {
                continue;
            }
            if (item instanceof DictDataVO) {
                result.add((DictDataVO) item);
                continue;
            }
            result.add(objectMapper.convertValue(item, DictDataVO.class));
        }
        return result;
    }

    private Map<String, List<DictDataVO>> convertMap(Map<?, ?> raw) {
        Map<String, List<DictDataVO>> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() == null) {
                continue;
            }
            String type = String.valueOf(entry.getKey());
            if (StringUtils.isBlank(type)) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof List) {
                result.put(type, convertList((List<?>) value));
            } else if (value != null) {
                List<?> list = objectMapper.convertValue(value, List.class);
                result.put(type, convertList(list));
            } else {
                result.put(type, Collections.emptyList());
            }
        }
        return result;
    }
}
