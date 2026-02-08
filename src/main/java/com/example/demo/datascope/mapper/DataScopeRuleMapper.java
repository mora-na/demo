package com.example.demo.datascope.mapper;

import com.example.demo.common.mybatis.MppBaseMapper;
import com.example.demo.datascope.entity.DataScopeRule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DataScopeRuleMapper extends MppBaseMapper<DataScopeRule> {

    @Select("select id, table_name, column_name, enabled from sys_data_scope_rule where enabled = 1")
    List<DataScopeRule> selectEnabledRules();
}
