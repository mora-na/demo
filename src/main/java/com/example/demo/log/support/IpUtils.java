package com.example.demo.log.support;

import javax.servlet.http.HttpServletRequest;

/**
 * IP 工具类。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public final class IpUtils {

    private static final String[] HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    private IpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String header : HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static String resolveLocation(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        if (isInternalIp(ip)) {
            return "内网IP";
        }
        return "未知";
    }

    public static boolean isInternalIp(String ip) {
        if (ip == null) {
            return false;
        }
        String val = ip.trim();
        if (val.startsWith("127.") || val.startsWith("0:0:0:0:0:0:0:1") || val.equals("::1")) {
            return true;
        }
        if (val.startsWith("10.")) {
            return true;
        }
        if (val.startsWith("192.168.")) {
            return true;
        }
        if (val.startsWith("172.")) {
            String[] parts = val.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException ignored) {
                    return false;
                }
            }
        }
        return false;
    }
}
