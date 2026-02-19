package com.example.demo.extension.executor;

import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.registry.DynamicApiMeta;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;

class DynamicApiExecutorRouterTest {

    @Test
    void selectMatchesRouteInOrder() {
        ThreadPoolTaskExecutor defaultExecutor = new ThreadPoolTaskExecutor();
        ThreadPoolTaskExecutor typeExecutor = new ThreadPoolTaskExecutor();
        ThreadPoolTaskExecutor apiExecutor = new ThreadPoolTaskExecutor();
        ThreadPoolTaskExecutor pathExecutor = new ThreadPoolTaskExecutor();

        Map<String, ThreadPoolTaskExecutor> executors = new LinkedHashMap<>();
        executors.put("type-exec", typeExecutor);
        executors.put("api-exec", apiExecutor);
        executors.put("path-exec", pathExecutor);

        DynamicApiProperties properties = new DynamicApiProperties();
        DynamicApiProperties.ExecutorRoute byType = new DynamicApiProperties.ExecutorRoute();
        byType.setExecutorId("type-exec");
        byType.setType("SQL");
        DynamicApiProperties.ExecutorRoute byApi = new DynamicApiProperties.ExecutorRoute();
        byApi.setExecutorId("api-exec");
        byApi.setApiId(42L);
        DynamicApiProperties.ExecutorRoute byPath = new DynamicApiProperties.ExecutorRoute();
        byPath.setExecutorId("path-exec");
        byPath.setPathPrefix("/ext/special");
        properties.setExecutorRoutes(Arrays.asList(byApi, byType, byPath));

        DynamicApiExecutorRouter router = new DynamicApiExecutorRouter(defaultExecutor, executors, properties);

        DynamicApiMeta meta = buildMeta(42L, "/ext/special/demo", "SQL");
        assertSame(apiExecutor, router.select(meta));
    }

    @Test
    void selectFallsBackToDefault() {
        ThreadPoolTaskExecutor defaultExecutor = new ThreadPoolTaskExecutor();
        DynamicApiExecutorRouter router = new DynamicApiExecutorRouter(defaultExecutor, Collections.emptyMap(), new DynamicApiProperties());
        DynamicApiMeta meta = buildMeta(1L, "/ext/any", "HTTP");
        assertSame(defaultExecutor, router.select(meta));
    }

    private DynamicApiMeta buildMeta(Long id, String path, String type) {
        DynamicApi api = new DynamicApi();
        api.setId(id);
        api.setPath(path);
        api.setMethod("GET");
        api.setType(type);
        return new DynamicApiMeta(api, type, DynamicApiAuthMode.INHERIT, null, null);
    }
}
