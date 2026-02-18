package com.example.demo.extension.adapter;

import com.example.demo.common.web.permission.PermissionBypassEvaluator;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.registry.DynamicApiMatch;
import com.example.demo.extension.registry.DynamicApiRegistry;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 动态接口 PUBLIC 模式权限绕过。
 */
@Component
public class DynamicApiPermissionBypassEvaluator implements PermissionBypassEvaluator {

    private final DynamicApiRegistry registry;

    public DynamicApiPermissionBypassEvaluator(DynamicApiRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean shouldBypass(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        DynamicApiMatch match = registry.match(request.getMethod(), request.getRequestURI());
        if (match == null || match.getMeta() == null) {
            return false;
        }
        return DynamicApiAuthMode.PUBLIC.equals(match.getMeta().getAuthMode());
    }
}
