package com.example.demo.config.support;

import com.example.demo.config.api.enums.ConfigValueType;

import java.io.Serializable;

/**
 * 解析后的配置值。
 */
public class ConfigValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String group;
    private final String key;
    private final String value;
    private final ConfigValueType type;
    private final Integer version;
    private final boolean hotUpdate;

    public ConfigValue(String group,
                       String key,
                       String value,
                       ConfigValueType type,
                       Integer version,
                       boolean hotUpdate) {
        this.group = group;
        this.key = key;
        this.value = value;
        this.type = type;
        this.version = version;
        this.hotUpdate = hotUpdate;
    }

    public String getGroup() {
        return group;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public ConfigValueType getType() {
        return type;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isHotUpdate() {
        return hotUpdate;
    }
}
