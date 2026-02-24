package com.example.demo.config.api.event;

import com.example.demo.config.api.enums.ConfigValueType;
import lombok.Getter;

import java.io.Serializable;

/**
 * 配置变更事件。
 */
@Getter
public class ConfigChangeEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String group;
    private final String key;
    private final String oldValue;
    private final String newValue;
    private final ConfigValueType type;
    private final Integer version;
    private final boolean hotUpdate;

    public ConfigChangeEvent(String group,
                             String key,
                             String oldValue,
                             String newValue,
                             ConfigValueType type,
                             Integer version,
                             boolean hotUpdate) {
        this.group = group;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.type = type;
        this.version = version;
        this.hotUpdate = hotUpdate;
    }

}
