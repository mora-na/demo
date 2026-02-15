package com.example.demo.common.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * 空实现的数据范围规则提供者，返回空映射。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
@ConditionalOnMissingBean(DataScopeRuleProvider.class)
public class NoopDataScopeRuleProvider implements DataScopeRuleProvider {

    /**
     * 返回空的表列映射。
     *
     * @return 空映射
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public Map<String, DataScopeRuleDefinition> getRuleMap() {
        return Collections.emptyMap();
    }
}
