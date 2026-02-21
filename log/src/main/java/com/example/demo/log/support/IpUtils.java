package com.example.demo.log.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.log.config.LogConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

/**
 * IP 工具类。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public final class IpUtils {

    private static final LogConstants DEFAULTS = new LogConstants();

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        LogConstants.Ip ipConstants = constants().getIp();
        List<String> headers = ipConstants.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return request.getRemoteAddr();
        }
        for (String header : headers) {
            if (StringUtils.isBlank(header)) {
                continue;
            }
            String ipHeader = request.getHeader(header);
            if (StringUtils.isBlank(ipHeader)) {
                continue;
            }
            String separator = ipConstants.getMultiIpSeparator();
            if (StringUtils.isBlank(separator)) {
                String candidate = ipHeader.trim();
                if (!isUnknownToken(candidate, ipConstants)) {
                    return candidate;
                }
                continue;
            }
            if (ipHeader.contains(separator)) {
                String[] parts = ipHeader.split(Pattern.quote(separator));
                for (String part : parts) {
                    String candidate = part == null ? null : part.trim();
                    if (StringUtils.isBlank(candidate)) {
                        continue;
                    }
                    if (!isUnknownToken(candidate, ipConstants)) {
                        return candidate;
                    }
                }
                continue;
            }
            String candidate = ipHeader.trim();
            if (!isUnknownToken(candidate, ipConstants)) {
                return candidate;
            }
        }
        return request.getRemoteAddr();
    }

    public static String resolveLocation(String ip) {
        if (StringUtils.isBlank(ip)) {
            return null;
        }
        LogConstants.Ip ipConstants = constants().getIp();
        if (isInternalIp(ip)) {
            return ipConstants.getInternalIpText();
        }
        return ipConstants.getUnknownLocationText();
    }

    public static boolean isInternalIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        LogConstants.Ip ipConstants = constants().getIp();
        String val = ip.trim();
        if (val.startsWith(ipConstants.getIpv4LoopbackPrefix())
                || val.startsWith(ipConstants.getIpv6LoopbackFull())
                || val.equals(ipConstants.getIpv6LoopbackShort())) {
            return true;
        }
        if (val.startsWith(ipConstants.getPrivateAPrefix())) {
            return true;
        }
        if (val.startsWith(ipConstants.getPrivateCPrefix())) {
            return true;
        }
        if (val.startsWith(ipConstants.getPrivateBPrefix())) {
            String[] parts = val.split(ipConstants.getIpv4SegmentSeparatorRegex());
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= ipConstants.getPrivateBSecondOctetMin()
                            && second <= ipConstants.getPrivateBSecondOctetMax();
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }

    private static LogConstants constants() {
        LogConstants bean = SpringContextHolder.getBean(LogConstants.class);
        return bean == null ? DEFAULTS : bean;
    }

    private static boolean isUnknownToken(String value, LogConstants.Ip ipConstants) {
        if (StringUtils.isBlank(value)) {
            return true;
        }
        return Strings.CI.equals(value, ipConstants.getUnknownToken());
    }
}
