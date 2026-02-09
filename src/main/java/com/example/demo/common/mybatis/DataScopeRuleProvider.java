package com.example.demo.common.mybatis;

import java.util.Map;

/**
 * 数据范围规则提供接口，返回表与列的映射关系。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public interface DataScopeRuleProvider {

    /**
     * 获取表名到数据范围列名的映射。
     *
     * @return 表列映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    Map<String, String> getTableColumnMap();
}
