package com.example.demo.config.api.listener;

import com.example.demo.config.api.event.ConfigChangeEvent;

/**
 * 配置变更监听器（本地模式）。
 */
public interface ConfigChangeListener {

    /**
     * 配置变更回调。
     */
    void onChange(ConfigChangeEvent event);
}
