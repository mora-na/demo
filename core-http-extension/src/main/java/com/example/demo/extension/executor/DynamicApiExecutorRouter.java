package com.example.demo.extension.executor;

import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.registry.DynamicApiMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 动态接口执行器路由。
 */
@Slf4j
@Component
public class DynamicApiExecutorRouter {

    private final ThreadPoolTaskExecutor defaultExecutor;
    private final Map<String, ThreadPoolTaskExecutor> routeExecutors;
    private final List<RouteRule> routes;

    public DynamicApiExecutorRouter(@Qualifier("dynamicApiTaskExecutor") ThreadPoolTaskExecutor defaultExecutor,
                                    @Qualifier("dynamicApiRouteExecutors") Map<String, ThreadPoolTaskExecutor> routeExecutors,
                                    DynamicApiProperties properties) {
        this.defaultExecutor = defaultExecutor;
        this.routeExecutors = routeExecutors == null ? Collections.emptyMap() : routeExecutors;
        this.routes = buildRoutes(properties == null ? null : properties.getExecutorRoutes());
    }

    public ThreadPoolTaskExecutor select(DynamicApiMeta meta) {
        if (meta == null || meta.getApi() == null) {
            return defaultExecutor;
        }
        for (RouteRule rule : routes) {
            if (!rule.matches(meta)) {
                continue;
            }
            ThreadPoolTaskExecutor executor = routeExecutors.get(rule.executorId);
            if (executor != null) {
                return executor;
            }
            log.warn("Dynamic api executor route ignored: missing executorId={}, apiId={}, type={}, path={}",
                    rule.executorId,
                    meta.getApi().getId(),
                    meta.getType(),
                    meta.getApi().getPath());
        }
        return defaultExecutor;
    }

    private List<RouteRule> buildRoutes(List<DynamicApiProperties.ExecutorRoute> routes) {
        if (routes == null || routes.isEmpty()) {
            return Collections.emptyList();
        }
        List<RouteRule> result = new ArrayList<>();
        for (DynamicApiProperties.ExecutorRoute route : routes) {
            if (route == null || StringUtils.isBlank(route.getExecutorId())) {
                continue;
            }
            result.add(new RouteRule(route));
        }
        return result.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(result);
    }

    private static class RouteRule {
        private final String executorId;
        private final String type;
        private final Long apiId;
        private final String pathPrefix;

        private RouteRule(DynamicApiProperties.ExecutorRoute route) {
            this.executorId = route.getExecutorId();
            this.type = StringUtils.trimToNull(route.getType());
            this.apiId = route.getApiId();
            String prefix = StringUtils.trimToNull(route.getPathPrefix());
            this.pathPrefix = prefix == null ? null : prefix;
        }

        private boolean matches(DynamicApiMeta meta) {
            if (meta == null || meta.getApi() == null) {
                return false;
            }
            if (apiId != null && !apiId.equals(meta.getApi().getId())) {
                return false;
            }
            if (type != null) {
                String metaType = meta.getType();
                if (metaType == null || !metaType.toLowerCase(Locale.ROOT).equals(type.toLowerCase(Locale.ROOT))) {
                    return false;
                }
            }
            if (pathPrefix != null) {
                String path = meta.getApi().getPath();
                if (path == null || !path.startsWith(pathPrefix)) {
                    return false;
                }
            }
            return true;
        }
    }
}
