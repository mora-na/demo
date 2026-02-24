package com.example.demo.config.facade;

import com.example.demo.config.api.facade.ConfigReadFacade;
import com.example.demo.config.service.ConfigQueryService;
import org.springframework.stereotype.Service;

/**
 * 配置读取对外契约实现。
 */
@Service
public class ConfigReadFacadeImpl implements ConfigReadFacade {

    private final ConfigQueryService configQueryService;

    public ConfigReadFacadeImpl(ConfigQueryService configQueryService) {
        this.configQueryService = configQueryService;
    }

    @Override
    public String getString(String group, String key) {
        return configQueryService.getString(group, key, null);
    }

    @Override
    public String getString(String group, String key, String defaultValue) {
        return configQueryService.getString(group, key, defaultValue);
    }

    @Override
    public Boolean getBoolean(String group, String key, Boolean defaultValue) {
        return configQueryService.getBoolean(group, key, defaultValue);
    }

    @Override
    public Integer getInteger(String group, String key, Integer defaultValue) {
        return configQueryService.getInteger(group, key, defaultValue);
    }

    @Override
    public Long getLong(String group, String key, Long defaultValue) {
        return configQueryService.getLong(group, key, defaultValue);
    }

    @Override
    public <T> T getObject(String group, String key, Class<T> type, T defaultValue) {
        return configQueryService.getObject(group, key, type, defaultValue);
    }
}
