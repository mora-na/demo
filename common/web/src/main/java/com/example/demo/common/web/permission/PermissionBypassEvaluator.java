package com.example.demo.common.web.permission;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限拦截绕过评估器。
 */
public interface PermissionBypassEvaluator {

    /**
     * 是否跳过权限拦截。
     *
     * @param request HTTP 请求
     * @return true 表示绕过
     */
    boolean shouldBypass(HttpServletRequest request);
}
