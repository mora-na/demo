package com.example.demo.config.support;

import com.example.demo.config.api.enums.ConfigValueType;

import java.io.Serializable;

/**
 * 缓存的配置值。
 */
public class ConfigCacheValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String group;
    private String key;
    private String value;
    private ConfigValueType type;
    private Integer version;
    private boolean hotUpdate;

    public ConfigCacheValue() {
    }

    public ConfigCacheValue(String group,
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

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ConfigValueType getType() {
        return type;
    }

    public void setType(ConfigValueType type) {
        this.type = type;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public boolean isHotUpdate() {
        return hotUpdate;
    }

    public void setHotUpdate(boolean hotUpdate) {
        this.hotUpdate = hotUpdate;
    }
}
