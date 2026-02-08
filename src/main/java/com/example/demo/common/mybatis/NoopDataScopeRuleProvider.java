package com.example.demo.common.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@ConditionalOnMissingBean(DataScopeRuleProvider.class)
public class NoopDataScopeRuleProvider implements DataScopeRuleProvider {

    @Override
    public Map<String, String> getTableColumnMap() {
        return Collections.emptyMap();
    }
}
