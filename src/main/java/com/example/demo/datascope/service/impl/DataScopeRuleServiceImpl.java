package com.example.demo.datascope.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.datascope.entity.DataScopeRule;
import com.example.demo.datascope.mapper.DataScopeRuleMapper;
import com.example.demo.datascope.service.DataScopeRuleService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据范围规则服务实现，负责装配表与字段映射。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
public class DataScopeRuleServiceImpl extends MppServiceImpl<DataScopeRuleMapper, DataScopeRule>
        implements DataScopeRuleService {

    /**
     * 查询启用规则并构建表->字段映射。
     *
     * @return 表名到字段名的映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public Map<String, String> getEnabledTableColumnMap() {
        List<DataScopeRule> rules = baseMapper.selectEnabledRules();
        Map<String, String> map = new HashMap<>();
        if (rules == null || rules.isEmpty()) {
            return map;
        }
        for (DataScopeRule rule : rules) {
            if (rule == null) {
                continue;
            }
            String table = rule.getTableName();
            String column = rule.getColumnName();
            if (table == null || column == null) {
                continue;
            }
            map.put(table.trim().toLowerCase(), column);
        }
        return map;
    }
}
