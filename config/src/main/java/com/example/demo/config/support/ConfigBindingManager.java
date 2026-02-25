package com.example.demo.config.support;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.config.ConfigBinding;
import com.example.demo.common.config.ConfigField;
import com.example.demo.config.api.enums.ConfigValueType;
import com.example.demo.config.api.event.ConfigChangeEvent;
import com.example.demo.config.api.facade.ConfigReadFacade;
import com.example.demo.config.config.ConfigConstants;
import com.example.demo.config.config.ConfigDefaultsProperties;
import com.example.demo.config.config.ConfigPrewarmProperties;
import com.example.demo.config.config.ConfigSeedProperties;
import com.example.demo.config.entity.SysConfig;
import com.example.demo.config.mapper.SysConfigMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 常量配置绑定与热更新管理器。
 */
@Component
public class ConfigBindingManager implements SmartInitializingSingleton {

    private final ApplicationContext applicationContext;
    private final ConfigReadFacade configReadFacade;
    private final SysConfigMapper configMapper;
    private final ConfigConstants configConstants;
    private final ConfigSeedProperties seedProperties;
    private final ConfigDefaultsProperties defaultsProperties;
    private final ConfigCryptoService cryptoService;
    private final ConfigCacheService cacheService;
    private final ConfigPrewarmProperties prewarmProperties;
    private final Environment environment;
    private final SqlSessionFactory sqlSessionFactory;
    private final ObjectMapper objectMapper;

    private final Map<String, List<BindingTarget>> groupTargets = new ConcurrentHashMap<>();

    public ConfigBindingManager(ApplicationContext applicationContext,
                                ConfigReadFacade configReadFacade,
                                SysConfigMapper configMapper,
                                ConfigConstants configConstants,
                                ConfigSeedProperties seedProperties,
                                ConfigDefaultsProperties defaultsProperties,
                                ConfigCryptoService cryptoService,
                                ConfigCacheService cacheService,
                                ConfigPrewarmProperties prewarmProperties,
                                Environment environment,
                                SqlSessionFactory sqlSessionFactory,
                                ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.configReadFacade = configReadFacade;
        this.configMapper = configMapper;
        this.configConstants = configConstants;
        this.seedProperties = seedProperties;
        this.defaultsProperties = defaultsProperties;
        this.cryptoService = cryptoService;
        this.cacheService = cacheService;
        this.prewarmProperties = prewarmProperties;
        this.environment = environment;
        this.sqlSessionFactory = sqlSessionFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterSingletonsInstantiated() {
        initialize();
    }

    private void initialize() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(ConfigBinding.class);
        if (beans == null || beans.isEmpty()) {
            return;
        }
        for (Object bean : beans.values()) {
            if (bean == null) {
                continue;
            }
            ConfigBinding binding = bean.getClass().getAnnotation(ConfigBinding.class);
            if (binding == null) {
                continue;
            }
            BindingTarget target = buildTarget(bean, binding);
            groupTargets.computeIfAbsent(target.group, key -> new ArrayList<>()).add(target);
            Map<String, SysConfig> existingConfigs = prefetchConfigs(target);
            seedDefaults(target, existingConfigs);
            prewarmCache(target, existingConfigs);
            applyOverrides(target, existingConfigs);
        }
    }

    @EventListener
    public void onConfigChange(ConfigChangeEvent event) {
        if (event == null) {
            return;
        }
        String group = event.getGroup();
        if (StringUtils.isBlank(group)) {
            return;
        }
        List<BindingTarget> targets = groupTargets.get(group);
        if (targets == null || targets.isEmpty()) {
            return;
        }
        String key = StringUtils.trimToNull(event.getKey());
        if (key == null) {
            return;
        }
        for (BindingTarget target : targets) {
            String prefix = target.prefix;
            if (!key.startsWith(prefix)) {
                continue;
            }
            String path = key.substring(prefix.length());
            if (path.isEmpty()) {
                continue;
            }
            PropertyMeta meta = target.properties.get(path);
            if (meta == null || !meta.hotUpdateEnabled) {
                continue;
            }
            refreshProperty(target, meta);
        }
    }

    private BindingTarget buildTarget(Object bean, ConfigBinding binding) {
        String group = StringUtils.trimToNull(binding.group());
        if (group == null) {
            group = configConstants.getGroup().getDefaultGroup();
        }
        String prefix = normalizePrefix(binding.prefix());
        BeanWrapper wrapper = new BeanWrapperImpl(bean);
        Map<String, PropertyMeta> properties = collectProperties(bean, wrapper, "", binding.hotUpdate(), binding.seed());
        Map<String, Object> defaults = new LinkedHashMap<>();
        for (PropertyMeta meta : properties.values()) {
            defaults.put(meta.path, cloneValue(meta.value, meta.genericType, meta.type));
        }
        return new BindingTarget(bean, wrapper, group, prefix, properties, defaults);
    }

    private void applyOverrides(BindingTarget target, Map<String, SysConfig> existingConfigs) {
        for (PropertyMeta meta : target.properties.values()) {
            refreshProperty(target, meta, existingConfigs);
        }
    }

    private void refreshProperty(BindingTarget target, PropertyMeta meta) {
        String path = meta.path;
        String key = target.prefix + path;
        String raw = configReadFacade.getString(target.group, key);
        if (raw == null) {
            Object fallback = target.defaults.get(path);
            if (fallback != null) {
                target.wrapper.setPropertyValue(path, cloneValue(fallback, meta.genericType, meta.type));
            }
            return;
        }
        Object converted = convertValue(raw, meta.genericType, meta.type);
        if (converted != null) {
            target.wrapper.setPropertyValue(path, converted);
        }
    }

    private void refreshProperty(BindingTarget target, PropertyMeta meta, Map<String, SysConfig> existingConfigs) {
        String path = meta.path;
        String key = target.prefix + path;
        String raw = resolveRawValue(target.group, key, existingConfigs);
        if (raw == null) {
            Object fallback = target.defaults.get(path);
            if (fallback != null) {
                target.wrapper.setPropertyValue(path, cloneValue(fallback, meta.genericType, meta.type));
            }
            return;
        }
        Object converted = convertValue(raw, meta.genericType, meta.type);
        if (converted != null) {
            target.wrapper.setPropertyValue(path, converted);
        }
    }

    private Map<String, SysConfig> prefetchConfigs(BindingTarget target) {
        if (target == null || target.properties == null || target.properties.isEmpty()) {
            return new HashMap<>();
        }
        List<String> keys = new ArrayList<>(target.properties.size());
        for (PropertyMeta meta : target.properties.values()) {
            keys.add(target.prefix + meta.path);
        }
        if (keys.isEmpty()) {
            return new HashMap<>();
        }
        List<SysConfig> configs = configMapper.selectList(Wrappers.lambdaQuery(SysConfig.class)
                .eq(SysConfig::getConfigGroup, target.group)
                .in(SysConfig::getConfigKey, keys));
        if (configs == null || configs.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, SysConfig> map = new HashMap<>(configs.size());
        for (SysConfig config : configs) {
            if (config == null || config.getConfigKey() == null) {
                continue;
            }
            map.put(config.getConfigKey(), config);
        }
        return map;
    }

    private void seedDefaults(BindingTarget target, Map<String, SysConfig> existingConfigs) {
        if (!isSeedEnabled()) {
            return;
        }
        List<PropertyMeta> seedMetas = new ArrayList<>();
        for (PropertyMeta meta : target.properties.values()) {
            if (meta.seedEnabled) {
                seedMetas.add(meta);
            }
        }
        if (seedMetas.isEmpty()) {
            return;
        }
        Map<String, SysConfig> existingMap = existingConfigs == null ? new HashMap<>() : existingConfigs;
        List<SysConfig> toInsert = new ArrayList<>();
        for (PropertyMeta meta : seedMetas) {
            String key = target.prefix + meta.path;
            SysConfig existing = existingMap.get(key);
            if (existing != null) {
                syncHotUpdate(existing, meta.hotUpdateEnabled);
                continue;
            }
            Object value = meta.value;
            if (value == null) {
                continue;
            }
            String raw = serializeValue(value, meta.genericType, meta.type);
            if (raw == null) {
                continue;
            }
            SysConfig config = new SysConfig();
            config.setConfigGroup(target.group);
            config.setConfigKey(key);
            config.setConfigValue(raw);
            config.setConfigType(resolveType(meta.genericType, meta.type).name());
            config.setConfigSchema(null);
            config.setConfigVersion(1);
            config.setStatus(configConstants.getStatus().getEnabled());
            config.setHotUpdate(meta.hotUpdateEnabled
                    ? configConstants.getHotUpdate().getEnabled()
                    : configConstants.getHotUpdate().getDisabled());
            config.setConfigSensitive(0);
            config.setRemark("seeded by config binding");
            toInsert.add(config);
        }
        batchInsert(toInsert);
        for (SysConfig inserted : toInsert) {
            if (inserted != null && inserted.getConfigKey() != null) {
                existingMap.put(inserted.getConfigKey(), inserted);
            }
        }
    }

    private void prewarmCache(BindingTarget target, Map<String, SysConfig> existingConfigs) {
        if (!isPrewarmEnabled() || cacheService == null || target == null || target.properties == null || target.properties.isEmpty()) {
            return;
        }
        List<ConfigCacheValue> hits = new ArrayList<>();
        List<String> misses = new ArrayList<>();
        for (PropertyMeta meta : target.properties.values()) {
            if (!shouldPrewarm(meta)) {
                continue;
            }
            String key = target.prefix + meta.path;
            SysConfig config = existingConfigs == null ? null : existingConfigs.get(key);
            if (config == null) {
                misses.add(key);
                continue;
            }
            boolean enabled = config.getStatus() != null && config.getStatus() == configConstants.getStatus().getEnabled();
            if (!enabled) {
                misses.add(key);
                continue;
            }
            String raw = config.getConfigValue();
            boolean sensitive = config.getConfigSensitive() != null && config.getConfigSensitive() == 1;
            String value = cryptoService == null ? raw : cryptoService.decryptIfNeeded(sensitive, raw);
            ConfigValueType type = ConfigValueType.from(config.getConfigType());
            if (type == null) {
                type = ConfigValueType.STRING;
            }
            boolean hotUpdate = config.getHotUpdate() != null && config.getHotUpdate() == configConstants.getHotUpdate().getEnabled();
            Integer version = config.getConfigVersion();
            hits.add(new ConfigCacheValue(target.group, key, value, type, version, hotUpdate));
        }
        cacheService.putAll(hits);
        cacheService.putMisses(target.group, misses);
    }

    private boolean isPrewarmEnabled() {
        return prewarmProperties == null || prewarmProperties.isEnabled();
    }

    private boolean shouldPrewarm(PropertyMeta meta) {
        if (prewarmProperties == null || prewarmProperties.getMode() == null) {
            return true;
        }
        ConfigPrewarmProperties.PrewarmMode mode = prewarmProperties.getMode();
        switch (mode) {
            case NONE:
                return false;
            case SEEDED:
                return meta.seedEnabled;
            case HOT:
                return meta.hotUpdateEnabled;
            case SEEDED_OR_HOT:
                return meta.seedEnabled || meta.hotUpdateEnabled;
            case ALL:
            default:
                return true;
        }
    }

    private void batchInsert(List<SysConfig> configs) {
        if (configs == null || configs.isEmpty()) {
            return;
        }
        if (sqlSessionFactory == null || configs.size() == 1) {
            for (SysConfig config : configs) {
                configMapper.insert(config);
            }
            return;
        }
        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            SysConfigMapper mapper = session.getMapper(SysConfigMapper.class);
            int batchSize = 200;
            int index = 0;
            for (SysConfig config : configs) {
                mapper.insert(config);
                index++;
                if (index % batchSize == 0) {
                    session.flushStatements();
                }
            }
            session.flushStatements();
            session.commit();
        }
    }

    private void syncHotUpdate(SysConfig existing, boolean hotUpdateEnabled) {
        if (existing == null || existing.getId() == null) {
            return;
        }
        Integer desired = hotUpdateEnabled
                ? configConstants.getHotUpdate().getEnabled()
                : configConstants.getHotUpdate().getDisabled();
        Integer current = existing.getHotUpdate();
        if (current != null && current.equals(desired)) {
            return;
        }
        SysConfig update = new SysConfig();
        update.setId(existing.getId());
        update.setHotUpdate(desired);
        configMapper.updateById(update);
    }

    private Map<String, PropertyMeta> collectProperties(Object bean,
                                                        BeanWrapper wrapper,
                                                        String pathPrefix,
                                                        boolean defaultHotUpdate,
                                                        boolean defaultSeed) {
        Map<String, PropertyMeta> result = new LinkedHashMap<>();
        Deque<PropertyCursor> queue = new ArrayDeque<>();
        queue.add(new PropertyCursor(bean, wrapper, pathPrefix, defaultHotUpdate, defaultSeed));
        while (!queue.isEmpty()) {
            PropertyCursor cursor = queue.poll();
            for (PropertyDescriptor descriptor : cursor.wrapper.getPropertyDescriptors()) {
                String name = descriptor.getName();
                if ("class".equals(name)) {
                    continue;
                }
                Class<?> type = descriptor.getPropertyType();
                if (type == null) {
                    continue;
                }
                String path = cursor.pathPrefix.isEmpty() ? name : cursor.pathPrefix + "." + name;
                Object value = cursor.wrapper.getPropertyValue(name);
                Field field = findField(cursor.bean.getClass(), name);
                boolean hotUpdateEnabled = resolveHotUpdate(cursor.defaultHotUpdate, field);
                boolean seedEnabled = resolveSeed(cursor.defaultSeed, field);
                if (isLeafType(type)) {
                    Type genericType = field == null ? type : field.getGenericType();
                    result.put(path, new PropertyMeta(path, type, genericType, value, hotUpdateEnabled, seedEnabled));
                } else if (value != null) {
                    queue.add(new PropertyCursor(value, new BeanWrapperImpl(value), path, hotUpdateEnabled, seedEnabled));
                }
            }
        }
        return result;
    }

    private boolean resolveHotUpdate(boolean defaultHotUpdate, Field field) {
        if (field == null) {
            return defaultHotUpdate;
        }
        ConfigField configField = field.getAnnotation(ConfigField.class);
        if (configField == null) {
            return defaultHotUpdate;
        }
        return configField.hotUpdate();
    }

    private boolean resolveSeed(boolean defaultSeed, Field field) {
        if (field == null) {
            return defaultSeed;
        }
        ConfigField configField = field.getAnnotation(ConfigField.class);
        if (configField == null) {
            return defaultSeed;
        }
        return configField.seed();
    }

    private String resolveRawValue(String group, String key, Map<String, SysConfig> existingConfigs) {
        String envValue = resolveEnvValue(group, key);
        if (envValue != null) {
            return envValue;
        }
        String dbValue = resolveDbValue(key, existingConfigs);
        if (dbValue != null) {
            return dbValue;
        }
        return resolveDefaultValue(group, key);
    }

    private String resolveEnvValue(String group, String key) {
        if (environment == null) {
            return null;
        }
        String propertyKey = "config." + group + "." + key;
        String value = environment.getProperty(propertyKey);
        if (value == null) {
            value = environment.getProperty("config." + key);
        }
        if (value == null) {
            return null;
        }
        if (cryptoService != null && cryptoService.isEncrypted(value)) {
            return cryptoService.decryptIfNeeded(true, value);
        }
        return value;
    }

    private String resolveDbValue(String key, Map<String, SysConfig> existingConfigs) {
        if (existingConfigs == null || existingConfigs.isEmpty()) {
            return null;
        }
        SysConfig config = existingConfigs.get(key);
        if (config == null) {
            return null;
        }
        boolean enabled = config.getStatus() != null && config.getStatus() == configConstants.getStatus().getEnabled();
        if (!enabled) {
            return null;
        }
        String raw = config.getConfigValue();
        boolean sensitive = config.getConfigSensitive() != null && config.getConfigSensitive() == 1;
        if (cryptoService == null) {
            return raw;
        }
        return cryptoService.decryptIfNeeded(sensitive, raw);
    }

    private String resolveDefaultValue(String group, String key) {
        if (defaultsProperties == null) {
            return null;
        }
        Map<String, String> items = defaultsProperties.getItems();
        if (items == null || items.isEmpty()) {
            return null;
        }
        String value = items.get(group + "." + key);
        if (value != null) {
            return value;
        }
        return items.get(key);
    }

    private boolean isSeedEnabled() {
        return seedProperties == null || seedProperties.isEnabled();
    }

    private boolean isLeafType(Class<?> type) {
        if (type.isPrimitive()) {
            return true;
        }
        if (String.class.isAssignableFrom(type)) {
            return true;
        }
        if (Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type) || Character.class.isAssignableFrom(type)) {
            return true;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return true;
        }
        if (type.isArray()) {
            return true;
        }
        if (java.util.Collection.class.isAssignableFrom(type) || java.util.Map.class.isAssignableFrom(type)) {
            return true;
        }
        Package pkg = type.getPackage();
        if (pkg != null) {
            String name = pkg.getName();
            if (name.startsWith("java.")) {
                return true;
            }
        }
        return false;
    }

    private String normalizePrefix(String prefix) {
        String value = StringUtils.trimToEmpty(prefix);
        if (value.isEmpty()) {
            return "";
        }
        return value.endsWith(".") ? value : value + ".";
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private Object convertValue(String raw, Type genericType, Class<?> targetType) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (String.class == targetType) {
            return raw;
        }
        if (targetType == int.class || targetType == Integer.class) {
            try {
                return Integer.parseInt(trimmed);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        if (targetType == long.class || targetType == Long.class) {
            try {
                return Long.parseLong(trimmed);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        if (targetType == double.class || targetType == Double.class) {
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            if ("true".equalsIgnoreCase(trimmed)) {
                return true;
            }
            if ("false".equalsIgnoreCase(trimmed)) {
                return false;
            }
            return null;
        }
        if (Enum.class.isAssignableFrom(targetType)) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Enum> enumType = (Class<? extends Enum>) targetType;
                return Enum.valueOf(enumType, trimmed.toUpperCase(Locale.ROOT));
            } catch (Exception ex) {
                return null;
            }
        }
        if (java.util.Collection.class.isAssignableFrom(targetType)
                || java.util.Map.class.isAssignableFrom(targetType)
                || targetType.isArray()) {
            return parseJson(trimmed, genericType, targetType);
        }
        if (looksLikeJson(trimmed)) {
            return parseJson(trimmed, genericType, targetType);
        }
        try {
            return objectMapper.convertValue(trimmed, objectMapper.constructType(genericType));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Object parseJson(String raw, Type genericType, Class<?> targetType) {
        try {
            return objectMapper.readValue(raw, objectMapper.constructType(genericType));
        } catch (Exception ex) {
            try {
                return objectMapper.readValue(raw, targetType);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String serializeValue(Object value, Type genericType, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (String.class == type) {
            return String.valueOf(value);
        }
        if (type.isPrimitive() || Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
            return String.valueOf(value);
        }
        if (Enum.class.isAssignableFrom(type)) {
            return String.valueOf(value);
        }
        if (java.util.Collection.class.isAssignableFrom(type)
                || java.util.Map.class.isAssignableFrom(type)
                || type.isArray()
                || looksLikeJson(String.valueOf(value))) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception ex) {
                return null;
            }
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private Object cloneValue(Object value, Type genericType, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (String.class == type || type.isPrimitive() || Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
            return value;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return value;
        }
        try {
            return objectMapper.convertValue(value, objectMapper.constructType(genericType));
        } catch (IllegalArgumentException ex) {
            return value;
        }
    }

    private ConfigValueType resolveType(Type genericType, Class<?> type) {
        if (String.class == type) {
            return ConfigValueType.STRING;
        }
        if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
            return ConfigValueType.NUMBER;
        }
        if (Boolean.class.isAssignableFrom(type) || boolean.class == type) {
            return ConfigValueType.BOOLEAN;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return ConfigValueType.STRING;
        }
        if (java.util.Collection.class.isAssignableFrom(type)
                || java.util.Map.class.isAssignableFrom(type)
                || type.isArray()) {
            return ConfigValueType.JSON;
        }
        return ConfigValueType.STRING;
    }

    private boolean looksLikeJson(String raw) {
        if (raw == null) {
            return false;
        }
        String trimmed = raw.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}"))
                || (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }

    private static class PropertyCursor {
        private final Object bean;
        private final BeanWrapper wrapper;
        private final String pathPrefix;
        private final boolean defaultHotUpdate;
        private final boolean defaultSeed;

        private PropertyCursor(Object bean, BeanWrapper wrapper, String pathPrefix, boolean defaultHotUpdate, boolean defaultSeed) {
            this.bean = bean;
            this.wrapper = wrapper;
            this.pathPrefix = pathPrefix;
            this.defaultHotUpdate = defaultHotUpdate;
            this.defaultSeed = defaultSeed;
        }
    }

    private static class BindingTarget {
        private final Object bean;
        private final BeanWrapper wrapper;
        private final String group;
        private final String prefix;
        private final Map<String, PropertyMeta> properties;
        private final Map<String, Object> defaults;

        private BindingTarget(Object bean,
                              BeanWrapper wrapper,
                              String group,
                              String prefix,
                              Map<String, PropertyMeta> properties,
                              Map<String, Object> defaults) {
            this.bean = bean;
            this.wrapper = wrapper;
            this.group = group;
            this.prefix = prefix;
            this.properties = properties;
            this.defaults = defaults;
        }
    }

    private static class PropertyMeta {
        private final String path;
        private final Class<?> type;
        private final Type genericType;
        private final Object value;
        private final boolean hotUpdateEnabled;
        private final boolean seedEnabled;

        private PropertyMeta(String path, Class<?> type, Type genericType, Object value, boolean hotUpdateEnabled, boolean seedEnabled) {
            this.path = path;
            this.type = type;
            this.genericType = genericType;
            this.value = value;
            this.hotUpdateEnabled = hotUpdateEnabled;
            this.seedEnabled = seedEnabled;
        }
    }
}
