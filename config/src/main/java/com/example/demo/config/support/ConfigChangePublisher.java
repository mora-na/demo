package com.example.demo.config.support;

import com.example.demo.config.api.event.ConfigChangeEvent;
import com.example.demo.config.api.listener.ConfigChangeListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 配置变更事件发布器。
 */
@Component
public class ConfigChangePublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final List<ConfigChangeListener> listeners;

    public ConfigChangePublisher(ApplicationEventPublisher eventPublisher,
                                 List<ConfigChangeListener> listeners) {
        this.eventPublisher = eventPublisher;
        this.listeners = listeners == null ? Collections.emptyList() : listeners;
    }

    public void publish(ConfigChangeEvent event) {
        if (event == null) {
            return;
        }
        for (ConfigChangeListener listener : listeners) {
            if (listener == null) {
                continue;
            }
            try {
                listener.onChange(event);
            } catch (Exception ignored) {
            }
        }
        eventPublisher.publishEvent(event);
    }
}
