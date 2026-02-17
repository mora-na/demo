package com.example.demo.extension.support;

import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.controller.DynamicDispatcherController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Set;

/**
 * 动态接口合法性校验。
 */
@Component
public class DynamicApiValidator {

    private final RequestMappingHandlerMapping handlerMapping;
    private final DynamicApiConstants constants;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public DynamicApiValidator(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
                               DynamicApiConstants constants) {
        this.handlerMapping = handlerMapping;
        this.constants = constants;
    }

    public void validatePathAndMethod(String method, String path) {
        if (StringUtils.isBlank(path) || StringUtils.isBlank(method)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getPathInvalid());
        }
        if (!path.startsWith(constants.getHttp().getExtPrefix())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getPathInvalid());
        }
        if (path.startsWith(constants.getHttp().getErrorPath())
                || path.startsWith(constants.getHttp().getActuatorPrefix())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getPathInvalid());
        }
        if (isConflictWithController(method, path)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getPathInvalid());
        }
    }

    private boolean isConflictWithController(String method, String path) {
        if (handlerMapping == null) {
            return false;
        }
        HttpMethod httpMethod = HttpMethod.resolve(method);
        for (RequestMappingInfo info : handlerMapping.getHandlerMethods().keySet()) {
            HandlerMethod handler = handlerMapping.getHandlerMethods().get(info);
            if (handler != null && DynamicDispatcherController.class.equals(handler.getBeanType())) {
                continue;
            }
            if (httpMethod != null && info.getMethodsCondition() != null
                    && !info.getMethodsCondition().getMethods().isEmpty()
                    && !info.getMethodsCondition().getMethods().contains(httpMethod)) {
                continue;
            }
            if (matchPath(info, path)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchPath(RequestMappingInfo info, String path) {
        if (info == null) {
            return false;
        }
        if (info.getPathPatternsCondition() != null
                && info.getPathPatternsCondition().getPatterns() != null
                && !info.getPathPatternsCondition().getPatterns().isEmpty()) {
            PathContainer container = PathContainer.parsePath(path);
            for (PathPattern pattern : info.getPathPatternsCondition().getPatterns()) {
                if (pattern != null && pattern.matches(container)) {
                    return true;
                }
            }
            return false;
        }
        if (info.getPatternsCondition() != null
                && info.getPatternsCondition().getPatterns() != null
                && !info.getPatternsCondition().getPatterns().isEmpty()) {
            Set<String> patterns = info.getPatternsCondition().getPatterns();
            for (String pattern : patterns) {
                if (antPathMatcher.match(pattern, path)) {
                    return true;
                }
            }
        }
        return false;
    }
}
