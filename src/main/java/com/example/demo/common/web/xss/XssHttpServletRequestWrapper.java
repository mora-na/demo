package com.example.demo.common.web.xss;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * XSS 请求包装器，对参数与请求头进行转义处理。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 构造函数，包装原始请求。
     *
     * @param request 原始请求
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取单个参数并进行 XSS 转义。
     *
     * @param name 参数名
     * @return 转义后的参数值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String getParameter(String name) {
        return XssCleaner.sanitize(super.getParameter(name));
    }

    /**
     * 获取参数数组并对每个值进行 XSS 转义。
     *
     * @param name 参数名
     * @return 转义后的参数值数组
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        String[] sanitized = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            sanitized[i] = XssCleaner.sanitize(values[i]);
        }
        return sanitized;
    }

    /**
     * 获取参数 Map 并对值数组进行 XSS 转义。
     *
     * @return 转义后的参数 Map
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> original = super.getParameterMap();
        if (original == null || original.isEmpty()) {
            return original;
        }
        Map<String, String[]> sanitized = new LinkedHashMap<>(original.size());
        for (Map.Entry<String, String[]> entry : original.entrySet()) {
            String[] values = entry.getValue();
            if (values == null) {
                sanitized.put(entry.getKey(), null);
                continue;
            }
            String[] escapedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                escapedValues[i] = XssCleaner.sanitize(values[i]);
            }
            sanitized.put(entry.getKey(), escapedValues);
        }
        return sanitized;
    }

    /**
     * 获取请求头并进行 XSS 转义。
     *
     * @param name 请求头名
     * @return 转义后的请求头值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String getHeader(String name) {
        return XssCleaner.sanitize(super.getHeader(name));
    }
}
