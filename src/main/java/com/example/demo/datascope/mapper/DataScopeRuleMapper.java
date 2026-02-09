package com.example.demo.datascope.mapper;

import com.example.demo.common.mybatis.MppBaseMapper;
import com.example.demo.datascope.entity.DataScopeRule;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据范围规则数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface DataScopeRuleMapper extends MppBaseMapper<DataScopeRule> {

    /**
     * 查询所有启用的数据范围规则。
     *
     * @return 规则列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Select("select id, table_name, column_name, enabled from sys_data_scope_rule where enabled = 1")
    List<DataScopeRule> selectEnabledRules();
}
