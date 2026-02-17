package com.example.demo.common.web.permission;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证绕过评估器，用于在过滤器层判断是否跳过鉴权。
 */
@FunctionalInterface
public interface AuthBypassEvaluator {

    boolean shouldBypass(HttpServletRequest request);
}
