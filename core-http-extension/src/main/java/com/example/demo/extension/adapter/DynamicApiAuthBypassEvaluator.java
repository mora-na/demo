package com.example.demo.extension.adapter;

import com.example.demo.common.web.permission.AuthBypassEvaluator;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.registry.DynamicApiMatch;
import com.example.demo.extension.registry.DynamicApiRegistry;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 动态接口 PUBLIC 模式鉴权绕过。
 */
@Component
public class DynamicApiAuthBypassEvaluator implements AuthBypassEvaluator {

    private final DynamicApiRegistry registry;

    public DynamicApiAuthBypassEvaluator(DynamicApiRegistry registry) {
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
