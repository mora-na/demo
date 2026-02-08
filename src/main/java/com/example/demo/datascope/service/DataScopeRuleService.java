package com.example.demo.datascope.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.datascope.entity.DataScopeRule;

import java.util.Map;

public interface DataScopeRuleService extends IMppService<DataScopeRule> {

    Map<String, String> getEnabledTableColumnMap();
}
