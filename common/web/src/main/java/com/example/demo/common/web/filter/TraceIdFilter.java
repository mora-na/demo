package com.example.demo.common.web.filter;

import com.example.demo.common.config.CommonConstants;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器，生成并绑定请求链路标识到 MDC。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component
public class TraceIdFilter implements Filter {

    private final CommonConstants systemConstants;

    public TraceIdFilter(CommonConstants systemConstants) {
        this.systemConstants = systemConstants;
    }

    /**
     * 为当前请求绑定 traceId 并在请求结束后清理。
     *
     * @param request  请求
     * @param response 响应
     * @param chain    过滤器链
     * @throws IOException      IO 异常
     * @throws ServletException Servlet 异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String mdcKey = systemConstants.getTrace().getMdcKey();
        try {
            MDC.put(mdcKey, UUID.randomUUID().toString()); // 可改为从请求头提取 traceId
            chain.doFilter(request, response);
        } finally {
            MDC.remove(mdcKey);
        }
    }
}
