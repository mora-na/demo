package com.example.demo.extension.api.handler;

import com.example.demo.extension.api.request.DynamicApiRequest;

/**
 * 动态接口处理器统一入口。
 */
public interface DynamicApiHandler {

    /**
     * 处理动态接口请求。
     *
     * @param request 动态接口请求上下文
     * @return 响应数据
     * @throws Exception 处理异常
     */
    Object handle(DynamicApiRequest request) throws Exception;
}
