package com.example.demo.config.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.config.dto.ConfigCreateRequest;
import com.example.demo.config.dto.ConfigQuery;
import com.example.demo.config.dto.ConfigUpdateRequest;
import com.example.demo.config.entity.SysConfig;
import com.example.demo.config.support.ConfigOperationResult;

/**
 * 配置管理服务。
 */
public interface ConfigManagerService {

    IPage<SysConfig> page(Page<SysConfig> page, ConfigQuery query);

    SysConfig getById(Long id);

    ConfigOperationResult create(ConfigCreateRequest request);

    ConfigOperationResult update(Long id, ConfigUpdateRequest request);

    boolean delete(Long id);

    void refreshCache(String group, String key);

    void refreshCache();
}
