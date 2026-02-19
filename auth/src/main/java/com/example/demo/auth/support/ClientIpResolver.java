package com.example.demo.auth.support;

import com.example.demo.common.config.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 客户端 IP 解析器，支持可配置的 Forwarded 头信任。
 */
@Component
public class ClientIpResolver {

    private final CommonConstants commonConstants;
    private final com.example.demo.auth.config.AuthProperties authProperties;

    public ClientIpResolver(CommonConstants commonConstants,
                            com.example.demo.auth.config.AuthProperties authProperties) {
        this.commonConstants = commonConstants;
        this.authProperties = authProperties;
    }

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        if (authProperties.getNetwork().isTrustForwardedHeaders()) {
            String forwarded = request.getHeader(commonConstants.getHttp().getForwardedForHeader());
            if (StringUtils.isNotBlank(forwarded)) {
                int comma = forwarded.indexOf(',');
                String ip = comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
                if (StringUtils.isNotBlank(ip)) {
                    return ip;
                }
            }
            String realIp = request.getHeader(commonConstants.getHttp().getRealIpHeader());
            if (StringUtils.isNotBlank(realIp)) {
                return realIp.trim();
            }
        }
        return StringUtils.trimToNull(request.getRemoteAddr());
    }
}
