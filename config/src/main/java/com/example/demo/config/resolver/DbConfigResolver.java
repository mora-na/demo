package com.example.demo.config.resolver;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.config.api.enums.ConfigValueType;
import com.example.demo.config.config.ConfigConstants;
import com.example.demo.config.entity.SysConfig;
import com.example.demo.config.mapper.SysConfigMapper;
import com.example.demo.config.support.*;
import org.apache.commons.lang3.StringUtils;

/**
 * 数据库 + 缓存配置解析器。
 */
public class DbConfigResolver implements ConfigResolver {

    private final SysConfigMapper configMapper;
    private final ConfigConstants constants;
    private final ConfigCacheService cacheService;
    private final ConfigCryptoService cryptoService;

    public DbConfigResolver(SysConfigMapper configMapper,
                            ConfigConstants constants,
                            ConfigCacheService cacheService,
                            ConfigCryptoService cryptoService) {
        this.configMapper = configMapper;
        this.constants = constants;
        this.cacheService = cacheService;
        this.cryptoService = cryptoService;
    }

    @Override
    public ConfigValue resolve(ConfigResolveRequest request) {
        if (request == null || request.getKey() == null) {
            return null;
        }
        ConfigKey key = request.getKey();
        String group = normalizeGroup(key.getGroup());
        ConfigCacheResult cached = cacheService.getResult(group, key.getKey());
        if (cached.isHit()) {
            ConfigCacheValue cacheValue = cached.getValue();
            return new ConfigValue(group, key.getKey(), cacheValue.getValue(), cacheValue.getType(), cacheValue.getVersion(), cacheValue.isHotUpdate());
        }
        if (cached.isMiss()) {
            return null;
        }
        SysConfig config = configMapper.selectOne(Wrappers.lambdaQuery(SysConfig.class)
                .eq(SysConfig::getConfigGroup, group)
                .eq(SysConfig::getConfigKey, key.getKey())
                .eq(SysConfig::getStatus, constants.getStatus().getEnabled()));
        if (config == null) {
            cacheService.putMiss(group, key.getKey());
            return null;
        }
        String raw = config.getConfigValue();
        boolean sensitive = config.getConfigSensitive() != null && config.getConfigSensitive() == 1;
        String value = cryptoService.decryptIfNeeded(sensitive, raw);
        ConfigValueType type = ConfigValueType.from(config.getConfigType());
        if (type == null) {
            type = ConfigValueType.STRING;
        }
        boolean hotUpdate = config.getHotUpdate() != null && config.getHotUpdate() == 1;
        Integer version = config.getConfigVersion();
        ConfigCacheValue cacheValue = new ConfigCacheValue(group, key.getKey(), value, type, version, hotUpdate);
        cacheService.put(cacheValue);
        return new ConfigValue(group, key.getKey(), value, type, version, hotUpdate);
    }

    private String normalizeGroup(String group) {
        String fallback = constants.getGroup().getDefaultGroup();
        return StringUtils.defaultIfBlank(group, fallback);
    }
}
