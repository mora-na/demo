package com.example.demo.common.web.support;

import com.example.demo.common.config.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析器，支持可信代理头配置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/19
 */
@Component("commonClientIpResolver")
public class ClientIpResolver {

    private final CommonConstants systemConstants;

    public ClientIpResolver(CommonConstants systemConstants) {
        this.systemConstants = systemConstants;
    }

    /**
     * 解析客户端 IP。
     *
     * @param request HTTP 请求
     * @return 客户端 IP
     */
    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        if (systemConstants != null && systemConstants.getHttp().isTrustForwardedHeaders()) {
            String forwarded = request.getHeader(systemConstants.getHttp().getForwardedForHeader());
            String ip = firstForwardedIp(forwarded);
            if (StringUtils.isNotBlank(ip)) {
                return ip;
            }
            String realIp = request.getHeader(systemConstants.getHttp().getRealIpHeader());
            if (StringUtils.isNotBlank(realIp)) {
                return realIp.trim();
            }
        }
        return request.getRemoteAddr();
    }

    private String firstForwardedIp(String forwarded) {
        if (StringUtils.isBlank(forwarded)) {
            return null;
        }
        int comma = forwarded.indexOf(',');
        String ip = comma > 0 ? forwarded.substring(0, comma) : forwarded;
        return StringUtils.trimToNull(ip);
    }
}
