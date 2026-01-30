package com.example.demo.framework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.demo.framework.service.IMppService;
import com.example.demo.framework.service.MppBaseMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MppServiceImpl<M extends MppBaseMapper<T>, T> extends com.github.jeffreyning.mybatisplus.service.MppServiceImpl<M, T> implements IMppService<T> {

    public static final String PAGE_NUM = "pageNum";
    public static final String PAGE_SIZE = "pageSize";


    public List<T> findPaginatedList(Map<String, String> paramMap) {
        try {
            int pageNum = Integer.parseInt(paramMap.get(PAGE_NUM));
            int pageSize = Integer.parseInt(paramMap.get(PAGE_SIZE));
            return findPaginatedList(paramMap, pageNum, pageSize);
        } catch (Exception e) {
            return findPaginatedList(Collections.emptyMap(), 1, 10);
        }
    }

    /**
     * 根据指定字段的键值对批量查询记录。 相当于 SQL: SELECT * FROM xxx WHERE key1 = val1 AND key2 = val2 ...
     *
     * @param paramMap 字段名-值的映射
     * @return 查询结果列表
     */
    public List<T> findPaginatedList(Map<String, String> paramMap, int pageNum, int pageSize) {
        try (Page<Object> ignored = PageHelper.startPage(pageNum, pageSize)) {
            List<T> list;
            if (paramMap == null || paramMap.isEmpty()) {
                list = this.baseMapper.selectList(new QueryWrapper<>());
            } else {
                // 转换成下划线 map
                Map<String, Object> dbColumnMap = new HashMap<>();
                for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                    if (StringUtils.isBlank(entry.getKey()) || StringUtils.isBlank(entry.getValue()) || PAGE_NUM.equals(entry.getKey()) || PAGE_SIZE.equals(entry.getKey())) {
                        continue;
                    }
                    String dbKey = StringUtils.camelToUnderline(entry.getKey());
                    dbColumnMap.put(dbKey, entry.getValue());
                }
                QueryWrapper<T> wrapper = new QueryWrapper<>();
                // 默认都用like
                dbColumnMap.forEach(wrapper::like);
                list = this.baseMapper.selectList(wrapper);
            }
            return list;
        }

    }

}
