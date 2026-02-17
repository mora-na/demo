package com.example.demo.extension.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 转发配置。
 */
@Data
public class HttpForwardConfig {

    /**
     * 目标 URL。
     */
    private String url;

    /**
     * 转发方法，空则使用原请求方法。
     */
    private String method;

    /**
     * 是否透传原始请求头。
     */
    private boolean passHeaders = true;

    /**
     * 是否透传 query 参数。
     */
    private boolean passQuery = true;

    /**
     * 额外请求头。
     */
    private Map<String, String> headers = new HashMap<>();
}
