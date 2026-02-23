package com.example.demo.common.datasource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * 按 app.datasource.mode 派生运行时配置，避免在 yml 中写复杂条件表达式。
 */
public class DatasourceModeEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "appDatasourceModeDerivedProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        DatasourceMode mode = resolveDatasourceMode(environment);
        String modeRaw = environment.getProperty("app.datasource.mode");
        boolean explicitMultiDatasource = modeRaw != null
                && !modeRaw.trim().isEmpty()
                && DatasourceMode.fromProperty(modeRaw) == DatasourceMode.MULTI_DATASOURCE;
        String singleSchemaName = environment.getProperty("app.datasource.single-schema-name", "demo");

        Map<String, Object> derived = new HashMap<>();
        boolean multiDatasource = mode == DatasourceMode.MULTI_DATASOURCE;
        boolean singleSchema = mode == DatasourceMode.SINGLE_DATASOURCE_SINGLE_SCHEMA;

        // 兼容保留旧开关，避免历史配置失效。
        putIfMissing(environment, derived, "app.datasource.multi-module-enabled", multiDatasource);

        // dynamic-datasource 自动配置开关：仅当 app.datasource.mode 显式为 multi-datasource 时启用
        derived.put("spring.datasource.dynamic.enabled", explicitMultiDatasource);

        // 单 schema SQL 重写开关
        putIfMissing(environment, derived, "app.datasource.sql-rewrite.enabled", singleSchema);
        putIfMissing(environment, derived, "app.datasource.sql-rewrite.target-schema", singleSchemaName);

        // Quartz 表前缀：单 schema 模式下不带 job.
        if (singleSchema) {
            putIfMissing(environment, derived, "spring.quartz.properties.org.quartz.jobStore.tablePrefix", "sys_quartz_");
            putIfMissing(environment, derived, "security.sql-guard.allowed-schemas", singleSchemaName);
        }

        if (!derived.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, derived));
        }
    }

    private DatasourceMode resolveDatasourceMode(ConfigurableEnvironment environment) {
        String modeRaw = environment.getProperty("app.datasource.mode");
        if (modeRaw != null && !modeRaw.trim().isEmpty()) {
            return DatasourceMode.fromProperty(modeRaw);
        }
        Boolean legacyMultiModuleEnabled = environment.getProperty("app.datasource.multi-module-enabled", Boolean.class);
        if (legacyMultiModuleEnabled != null && !legacyMultiModuleEnabled) {
            return DatasourceMode.SINGLE_DATASOURCE_MULTI_SCHEMA;
        }
        return DatasourceMode.MULTI_DATASOURCE;
    }

    @Override
    public int getOrder() {
        // 需要在 ConfigDataEnvironmentPostProcessor 之后执行，确保可读取 application*.yml 中的配置。
        return Ordered.HIGHEST_PRECEDENCE + 50;
    }

    private void putIfMissing(ConfigurableEnvironment environment, Map<String, Object> derived, String key, Object value) {
        if (!environment.containsProperty(key)) {
            derived.put(key, value);
        }
    }
}
