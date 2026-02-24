package com.example.demo.config.support;

import java.io.Serializable;
import java.util.Objects;

/**
 * 配置键。
 */
public class ConfigKey implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String group;
    private final String key;

    public ConfigKey(String group, String key) {
        this.group = group;
        this.key = key;
    }

    public String getGroup() {
        return group;
    }

    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigKey)) {
            return false;
        }
        ConfigKey configKey = (ConfigKey) o;
        return Objects.equals(group, configKey.group) && Objects.equals(key, configKey.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, key);
    }

    @Override
    public String toString() {
        return (group == null ? "" : group) + ":" + key;
    }
}
