package com.example.demo.framework;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.pagehelper.PageInfo;

import java.util.*;

public class MppServiceImpl<M extends MppBaseMapper<T>, T> extends com.github.jeffreyning.mybatisplus.service.MppServiceImpl<M, T> implements IMppService<T> {

//    public PaginatedList<T> findPaginatedList(Map<String, Object> paramMap) {
//        if (!Objects.isNull(paramMap)) {
//            return findPaginatedList(paramMap.get("columnMap"),paramMap.get("pageNum"),paramMap.get("pageSize"));
//        } else {
//            return findPaginatedList(java.util.Collections.emptyMap(),1,10);
//        }
//    }
//
//    /**
//     * 根据指定字段的键值对批量查询记录。
//     * 相当于 SQL: SELECT * FROM xxx WHERE key1 = val1 AND key2 = val2 ...
//     *
//     * @param columnMap 字段名-值的映射
//     * @return 查询结果列表
//     */
//    public PaginatedList<T> findPaginatedList(Map<String, String> columnMap, int pageNum, int pageSize) {
//        PageUtils.startPage(pageNum,pageSize);
//        List<T> list;
//        if (Collections.isEmpty(columnMap)) {
//            list = this.baseMapper.selectList(new QueryWrapper<>());
//        } else {
//            // 转换成下划线 map
//            Map<String, Object> dbColumnMap = new HashMap<>();
//            for (Map.Entry<String, String> entry : columnMap.entrySet()) {
//                if (StringUtils.isBlank(entry.getKey()) || Objects.isNull(entry.getValue()) || StringUtils.isBlank(entry.getValue())) {
//                    continue;
//                }
//                String dbKey = StringUtils.camelToUnderline(entry.getKey());
//                dbColumnMap.put(dbKey, entry.getValue());
//            }
//            QueryWrapper<T> wrapper = new QueryWrapper<>();
//            // 默认都用like
//            dbColumnMap.forEach(wrapper::like);
//            //list = this.baseMapper.selectByMap(dbColumnMap);
//            list = this.baseMapper.selectList(wrapper);
//        }
//        return new PaginatedList<T>().setDataList(list).setTotal(new PageInfo(list).getTotal());
//    }

}
