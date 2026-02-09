package com.example.demo.datascope.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.datascope.entity.DataScopeRule;

import java.util.Map;

/**
 * 数据范围规则服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface DataScopeRuleService extends IMppService<DataScopeRule> {

    /**
     * 获取启用规则的表->字段映射。
     *
     * @return 表名到字段名的映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    Map<String, String> getEnabledTableColumnMap();
}
