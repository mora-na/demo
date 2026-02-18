package com.example.demo.extension.support;

import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.executor.ExecuteStrategy;
import com.example.demo.extension.executor.ExecuteStrategyFactory;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 动态接口元数据构建器。
 */
@Component
public class DynamicApiMetaBuilder {

    private final ObjectMapper objectMapper;
    private final DynamicApiConstants constants;
    private final ExecuteStrategyFactory strategyFactory;
    private final PathPatternParser pathPatternParser = new PathPatternParser();

    public DynamicApiMetaBuilder(ObjectMapper objectMapper,
                                 DynamicApiConstants constants,
                                 ExecuteStrategyFactory strategyFactory) {
        this.objectMapper = objectMapper;
        this.constants = constants;
        this.strategyFactory = strategyFactory;
    }

    public DynamicApiMeta build(DynamicApi api) {
        if (api == null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        String method = normalizeMethod(api.getMethod());
        String path = normalizePath(api.getPath());
        if (StringUtils.isBlank(path) || !path.startsWith(constants.getHttp().getExtPrefix())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getPathInvalid());
        }
        if (!isSupportedMethod(method)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getMethodInvalid());
        }
        String type = strategyFactory.normalizeType(api.getType());
        if (StringUtils.isBlank(type)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getTypeInvalid());
        }
        ExecuteStrategy strategy = strategyFactory.get(type);
        if (strategy == null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getTypeInvalid());
        }
        DynamicApiAuthMode authMode = DynamicApiAuthMode.from(api.getAuthMode());
        if (authMode == null) {
            authMode = DynamicApiAuthMode.INHERIT;
        }
        api.setMethod(method);
        api.setPath(path);
        api.setType(type);
        api.setAuthMode(authMode.name());
        Object config = parseConfig(strategy, api.getConfig());
        PathPattern pathPattern = null;
        if (DynamicApiPathUtils.isPatternPath(path)) {
            try {
                pathPattern = pathPatternParser.parse(path);
            } catch (Exception ex) {
                throw new DynamicApiException(constants.getController().getBadRequestCode(),
                        constants.getMessage().getPathInvalid());
            }
        }
        return new DynamicApiMeta(api, type, authMode, config, pathPattern);
    }

    private Object parseConfig(ExecuteStrategy strategy, String configJson) {
        try {
            return strategy.parseConfig(configJson, objectMapper);
        } catch (DynamicApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
    }

    private String normalizeMethod(String method) {
        return method == null ? "" : method.trim().toUpperCase();
    }

    private String normalizePath(String path) {
        if (path == null) {
            return "";
        }
        String trimmed = path.trim();
        if (!trimmed.startsWith("/")) {
            return "/" + trimmed;
        }
        return trimmed;
    }

    private boolean isSupportedMethod(String method) {
        if (StringUtils.isBlank(method)) {
            return false;
        }
        return constants.getHttp().getSupportedMethods().contains(method.trim().toUpperCase());
    }
}
