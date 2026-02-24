package com.example.demo.config.support;

import com.example.demo.config.config.ConfigConstants;
import org.apache.commons.lang3.StringUtils;

/**
 * 缓存 Key 构造器。
 */
public class ConfigCacheKeyBuilder {

    private final ConfigConstants constants;

    public ConfigCacheKeyBuilder(ConfigConstants constants) {
        this.constants = constants;
    }

    public String build(String group, String key) {
        String prefix = constants.getCache().getKeyPrefix();
        String normalizedGroup = StringUtils.defaultIfBlank(group, constants.getGroup().getDefaultGroup());
        return prefix + normalizedGroup + ":" + key;
    }
}
