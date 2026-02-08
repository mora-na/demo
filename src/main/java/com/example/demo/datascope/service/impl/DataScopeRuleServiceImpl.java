package com.example.demo.datascope.service.impl;

import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.datascope.entity.DataScopeRule;
import com.example.demo.datascope.mapper.DataScopeRuleMapper;
import com.example.demo.datascope.service.DataScopeRuleService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataScopeRuleServiceImpl extends MppServiceImpl<DataScopeRuleMapper, DataScopeRule>
        implements DataScopeRuleService {

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
