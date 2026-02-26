package com.example.demo.config.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.config.api.enums.ConfigValueType;
import com.example.demo.config.api.event.ConfigChangeEvent;
import com.example.demo.config.config.ConfigConstants;
import com.example.demo.config.dto.ConfigCreateRequest;
import com.example.demo.config.dto.ConfigQuery;
import com.example.demo.config.dto.ConfigUpdateRequest;
import com.example.demo.config.entity.SysConfig;
import com.example.demo.config.mapper.SysConfigMapper;
import com.example.demo.config.service.ConfigManagerService;
import com.example.demo.config.support.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配置管理服务实现。
 */
@Service
public class ConfigManagerServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ConfigManagerService {

    private final ConfigConstants constants;
    private final ConfigCacheService cacheService;
    private final ConfigCryptoService cryptoService;
    private final ConfigSchemaValidator schemaValidator;
    private final ObjectMapper objectMapper;
    private final ConfigChangePublisher changePublisher;
    private final ConfigChangeBroadcaster changeBroadcaster;
    private final ConfigChangeLogService changeLogService;
    private final com.example.demo.common.cluster.NodeIdProvider nodeIdProvider;

    public ConfigManagerServiceImpl(ConfigConstants constants,
                                    ConfigCacheService cacheService,
                                    ConfigCryptoService cryptoService,
                                    ConfigSchemaValidator schemaValidator,
                                    ObjectMapper objectMapper,
                                    ConfigChangePublisher changePublisher,
                                    ConfigChangeBroadcaster changeBroadcaster,
                                    ConfigChangeLogService changeLogService,
                                    com.example.demo.common.cluster.NodeIdProvider nodeIdProvider) {
        this.constants = constants;
        this.cacheService = cacheService;
        this.cryptoService = cryptoService;
        this.schemaValidator = schemaValidator;
        this.objectMapper = objectMapper;
        this.changePublisher = changePublisher;
        this.changeBroadcaster = changeBroadcaster;
        this.changeLogService = changeLogService;
        this.nodeIdProvider = nodeIdProvider;
    }

    @Override
    public IPage<SysConfig> page(Page<SysConfig> page, ConfigQuery query) {
        if (page == null) {
            page = query == null ? new Page<>(1, 10) : query.buildPage();
        }
        return this.page(page, Wrappers.lambdaQuery(SysConfig.class)
                .like(StringUtils.isNotBlank(query == null ? null : query.getKey()), SysConfig::getConfigKey, query == null ? null : query.getKey())
                .like(StringUtils.isNotBlank(query == null ? null : query.getGroup()), SysConfig::getConfigGroup, query == null ? null : query.getGroup())
                .eq(StringUtils.isNotBlank(query == null ? null : query.getType()), SysConfig::getConfigType, query == null ? null : query.getType())
                .eq(query != null && query.getStatus() != null, SysConfig::getStatus, query == null ? null : query.getStatus())
                .eq(query != null && query.getHotUpdate() != null, SysConfig::getHotUpdate, query == null ? null : query.getHotUpdate())
                .eq(query != null && query.getSensitive() != null, SysConfig::getConfigSensitive, query == null ? null : query.getSensitive())
                .orderByDesc(SysConfig::getUpdateTime)
                .orderByDesc(SysConfig::getId));
    }

    @Override
    public SysConfig getById(Long id) {
        return super.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigOperationResult create(ConfigCreateRequest request) {
        if (request == null) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigValueInvalid());
        }
        String group = normalizeGroup(request.getGroup());
        String key = StringUtils.trimToNull(request.getKey());
        if (key == null) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigValueInvalid());
        }
        if (exists(group, key, null)) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigKeyExists());
        }
        ConfigValueType type = normalizeType(request.getType());
        Integer status = normalizeStatus(request.getStatus());
        Integer hotUpdate = normalizeHotUpdate(request.getHotUpdate());
        Integer sensitive = normalizeSensitive(request.getSensitive());
        String schema = normalizeSchema(request.getSchema());
        NormalizedValue normalized = normalizeValue(type, request.getValue(), schema);
        if (!normalized.success) {
            return ConfigOperationResult.failed(normalized.messageKey);
        }
        String stored = cryptoService.encryptIfNeeded(sensitive == 1, normalized.value);
        SysConfig config = new SysConfig();
        config.setConfigGroup(group);
        config.setConfigKey(key);
        config.setConfigValue(stored);
        config.setConfigType(type.name());
        config.setConfigSchema(schema);
        config.setStatus(status);
        config.setHotUpdate(hotUpdate);
        config.setConfigSensitive(sensitive);
        config.setConfigVersion(1);
        config.setRemark(StringUtils.trimToNull(request.getRemark()));
        if (!this.save(config)) {
            return ConfigOperationResult.failed(constants.getMessage().getCommonUpdateFailed());
        }
        cacheService.evict(group, key);
        recordChange(null, config, null, stored, ConfigChangeLogService.TYPE_CREATE);
        publishIfHot(null, config, normalized.value);
        return ConfigOperationResult.success(config);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfigOperationResult update(Long id, ConfigUpdateRequest request) {
        if (id == null || request == null) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigValueInvalid());
        }
        SysConfig existing = this.getById(id);
        if (existing == null) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigNotFound());
        }
        String requestedKey = StringUtils.trimToNull(request.getKey());
        if (requestedKey == null) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigValueInvalid());
        }
        String existingKey = existing.getConfigKey();
        if (existingKey != null && !existingKey.equals(requestedKey)) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigKeyImmutable());
        }
        String requestedGroup = StringUtils.trimToNull(request.getGroup());
        String existingGroup = normalizeGroup(existing.getConfigGroup());
        if (requestedGroup != null && !requestedGroup.equals(existingGroup)) {
            return ConfigOperationResult.failed(constants.getMessage().getConfigGroupImmutable());
        }
        String group = existingGroup;
        String key = existingKey == null ? requestedKey : existingKey;
        ConfigValueType type = request.getType() == null ? ConfigValueType.from(existing.getConfigType()) : normalizeType(request.getType());
        if (type == null) {
            type = ConfigValueType.STRING;
        }
        Integer status = request.getStatus() == null ? existing.getStatus() : normalizeStatus(request.getStatus());
        Integer hotUpdate = request.getHotUpdate() == null ? existing.getHotUpdate() : normalizeHotUpdate(request.getHotUpdate());
        Integer sensitive = request.getSensitive() == null ? existing.getConfigSensitive() : normalizeSensitive(request.getSensitive());
        String schema = request.getSchema() == null ? existing.getConfigSchema() : normalizeSchema(request.getSchema());
        NormalizedValue normalized = normalizeValue(type, request.getValue(), schema);
        if (!normalized.success) {
            return ConfigOperationResult.failed(normalized.messageKey);
        }
        String stored = cryptoService.encryptIfNeeded(sensitive == 1, normalized.value);
        SysConfig update = new SysConfig();
        update.setId(id);
        update.setConfigGroup(group);
        update.setConfigKey(key);
        update.setConfigValue(stored);
        update.setConfigType(type.name());
        update.setConfigSchema(schema);
        update.setStatus(status);
        update.setHotUpdate(hotUpdate);
        update.setConfigSensitive(sensitive);
        update.setConfigVersion(nextVersion(existing.getConfigVersion()));
        update.setRemark(StringUtils.trimToNull(request.getRemark()));
        if (!this.updateById(update)) {
            return ConfigOperationResult.failed(constants.getMessage().getCommonUpdateFailed());
        }
        cacheService.evict(existing.getConfigGroup(), existing.getConfigKey());
        cacheService.evict(group, key);
        recordChange(existing, update, existing.getConfigValue(), stored, ConfigChangeLogService.TYPE_UPDATE);
        String oldValue = cryptoService.decryptIfNeeded(existing.getConfigSensitive() != null && existing.getConfigSensitive() == 1, existing.getConfigValue());
        boolean oldEnabled = existing.getStatus() != null && existing.getStatus() == constants.getStatus().getEnabled();
        boolean newEnabled = status != null && status == constants.getStatus().getEnabled();
        SysConfig merged = merge(existing, update);
        SysConfig refreshed = this.getById(id);
        if (refreshed != null) {
            merged = refreshed;
        }
        if (oldEnabled && !newEnabled) {
            publishDisable(merged, oldValue);
        } else {
            publishIfHot(oldValue, merged, normalized.value);
        }
        return ConfigOperationResult.success(merged);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        SysConfig existing = this.getById(id);
        if (existing == null) {
            return false;
        }
        boolean removed = this.removeById(id);
        if (removed) {
            cacheService.evict(existing.getConfigGroup(), existing.getConfigKey());
            recordChange(existing, null, existing.getConfigValue(), null, ConfigChangeLogService.TYPE_DELETE);
            String oldValue = cryptoService.decryptIfNeeded(existing.getConfigSensitive() != null && existing.getConfigSensitive() == 1, existing.getConfigValue());
            publishDisable(existing, oldValue);
        }
        return removed;
    }

    @Override
    public void refreshCache(String group, String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        cacheService.evict(group, key);
    }

    @Override
    public void refreshCache() {
        List<SysConfig> configs = this.list();
        if (configs == null || configs.isEmpty()) {
            return;
        }
        for (SysConfig config : configs) {
            if (config == null) {
                continue;
            }
            cacheService.evict(config.getConfigGroup(), config.getConfigKey());
        }
    }

    private boolean exists(String group, String key, Long excludeId) {
        return this.count(Wrappers.lambdaQuery(SysConfig.class)
                .eq(SysConfig::getConfigGroup, group)
                .eq(SysConfig::getConfigKey, key)
                .ne(excludeId != null, SysConfig::getId, excludeId)) > 0;
    }

    private String normalizeGroup(String group) {
        String fallback = constants.getGroup().getDefaultGroup();
        return StringUtils.defaultIfBlank(group, fallback);
    }

    private ConfigValueType normalizeType(ConfigValueType type) {
        return type == null ? ConfigValueType.STRING : type;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return constants.getStatus().getEnabled();
        }
        return status;
    }

    private Integer normalizeHotUpdate(Integer hotUpdate) {
        if (hotUpdate == null) {
            return constants.getHotUpdate().getDisabled();
        }
        return hotUpdate;
    }

    private Integer normalizeSensitive(Integer sensitive) {
        if (sensitive == null) {
            return 0;
        }
        return sensitive;
    }

    private String normalizeSchema(String schema) {
        String trimmed = StringUtils.trimToNull(schema);
        return trimmed == null ? null : trimmed;
    }

    private int nextVersion(Integer current) {
        if (current == null || current < 0) {
            return 1;
        }
        return current + 1;
    }

    private NormalizedValue normalizeValue(ConfigValueType type, String raw, String schema) {
        if (raw == null) {
            return NormalizedValue.failed(constants.getMessage().getConfigValueInvalid());
        }
        String trimmed = raw.trim();
        if (type == ConfigValueType.JSON) {
            try {
                JsonNode node = objectMapper.readTree(trimmed);
                ConfigSchemaValidator.ValidationResult result = schemaValidator.validate(schema, node);
                if (!result.isValid()) {
                    String messageKey = "schema.invalid".equals(result.getMessage())
                            ? constants.getMessage().getConfigSchemaInvalid()
                            : constants.getMessage().getConfigValueInvalid();
                    return NormalizedValue.failed(messageKey);
                }
                String normalized = objectMapper.writeValueAsString(node);
                return NormalizedValue.success(normalized);
            } catch (Exception ex) {
                return NormalizedValue.failed(constants.getMessage().getConfigValueInvalid());
            }
        }
        if (type == ConfigValueType.BOOLEAN) {
            if (!"true".equalsIgnoreCase(trimmed) && !"false".equalsIgnoreCase(trimmed)) {
                return NormalizedValue.failed(constants.getMessage().getConfigValueInvalid());
            }
            return NormalizedValue.success(trimmed.toLowerCase());
        }
        if (type == ConfigValueType.NUMBER) {
            try {
                new BigDecimal(trimmed);
                return NormalizedValue.success(trimmed);
            } catch (Exception ex) {
                return NormalizedValue.failed(constants.getMessage().getConfigValueInvalid());
            }
        }
        return NormalizedValue.success(trimmed);
    }

    private void publishIfHot(String oldValue, SysConfig config, String newValue) {
        if (config == null) {
            return;
        }
        boolean enabled = config.getStatus() != null && config.getStatus() == constants.getStatus().getEnabled();
        boolean hot = config.getHotUpdate() != null && config.getHotUpdate() == constants.getHotUpdate().getEnabled();
        if (!enabled || !hot) {
            return;
        }
        ConfigChangeEvent event = new ConfigChangeEvent(
                config.getConfigGroup(),
                config.getConfigKey(),
                oldValue,
                newValue,
                ConfigValueType.from(config.getConfigType()),
                config.getConfigVersion(),
                true
        );
        changePublisher.publish(event);
        if (changeBroadcaster != null) {
            changeBroadcaster.broadcast(event);
        }
    }

    private void publishDisable(SysConfig config, String oldValue) {
        if (config == null) {
            return;
        }
        boolean hot = config.getHotUpdate() != null && config.getHotUpdate() == constants.getHotUpdate().getEnabled();
        if (!hot) {
            return;
        }
        ConfigChangeEvent event = new ConfigChangeEvent(
                config.getConfigGroup(),
                config.getConfigKey(),
                oldValue,
                null,
                ConfigValueType.from(config.getConfigType()),
                config.getConfigVersion(),
                true
        );
        changePublisher.publish(event);
        if (changeBroadcaster != null) {
            changeBroadcaster.broadcast(event);
        }
    }

    private void recordChange(SysConfig before,
                              SysConfig after,
                              String oldStoredValue,
                              String newStoredValue,
                              String type) {
        if (changeLogService == null) {
            return;
        }
        String nodeId = nodeIdProvider == null ? null : nodeIdProvider.get();
        changeLogService.record(before, after, oldStoredValue, newStoredValue, type, nodeId);
    }

    private SysConfig merge(SysConfig existing, SysConfig update) {
        SysConfig merged = new SysConfig();
        merged.setId(existing.getId());
        merged.setConfigGroup(update.getConfigGroup() == null ? existing.getConfigGroup() : update.getConfigGroup());
        merged.setConfigKey(update.getConfigKey() == null ? existing.getConfigKey() : update.getConfigKey());
        merged.setConfigValue(update.getConfigValue() == null ? existing.getConfigValue() : update.getConfigValue());
        merged.setConfigType(update.getConfigType() == null ? existing.getConfigType() : update.getConfigType());
        merged.setConfigSchema(update.getConfigSchema() == null ? existing.getConfigSchema() : update.getConfigSchema());
        merged.setStatus(update.getStatus() == null ? existing.getStatus() : update.getStatus());
        merged.setHotUpdate(update.getHotUpdate() == null ? existing.getHotUpdate() : update.getHotUpdate());
        merged.setConfigSensitive(update.getConfigSensitive() == null ? existing.getConfigSensitive() : update.getConfigSensitive());
        merged.setConfigVersion(update.getConfigVersion() == null ? existing.getConfigVersion() : update.getConfigVersion());
        return merged;
    }

    private static class NormalizedValue {
        private final boolean success;
        private final String value;
        private final String messageKey;

        private NormalizedValue(boolean success, String value, String messageKey) {
            this.success = success;
            this.value = value;
            this.messageKey = messageKey;
        }

        static NormalizedValue success(String value) {
            return new NormalizedValue(true, value, null);
        }

        static NormalizedValue failed(String messageKey) {
            return new NormalizedValue(false, null, messageKey);
        }
    }
}
