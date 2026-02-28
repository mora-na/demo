package com.example.demo.config.api.facade;

/**
 * 配置读取对外契约。
 */
public interface ConfigReadFacade {

    String getString(String group, String key);

    String getString(String group, String key, String defaultValue);

    Boolean getBoolean(String group, String key, Boolean defaultValue);

    Integer getInteger(String group, String key, Integer defaultValue);

    Long getLong(String group, String key, Long defaultValue);

    <T> T getObject(String group, String key, Class<T> type, T defaultValue);
}
