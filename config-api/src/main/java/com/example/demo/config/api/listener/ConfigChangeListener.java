package com.example.demo.config.api.listener;

import com.example.demo.config.api.event.ConfigChangeEvent;

/**
 * 配置变更监听器（本地模式）。
 */
public interface ConfigChangeListener {

    /**
     * 兼容简单监听签名。
     */
    void onChange(String key, String newValue);

    /**
     * 完整事件回调（默认委托到简化签名）。
     */
    default void onChange(ConfigChangeEvent event) {
        if (event == null) {
            return;
        }
        onChange(event.getKey(), event.getNewValue());
    }
}
