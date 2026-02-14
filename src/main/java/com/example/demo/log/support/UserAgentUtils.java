package com.example.demo.log.support;

import org.apache.commons.lang3.StringUtils;

/**
 * User-Agent 简易解析工具。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public final class UserAgentUtils {

    private UserAgentUtils() {
    }

    public static UserAgentInfo parse(String userAgent) {
        UserAgentInfo info = new UserAgentInfo();
        if (StringUtils.isBlank(userAgent)) {
            return info;
        }
        String ua = userAgent.toLowerCase();
        info.setBrowser(resolveBrowser(ua));
        info.setOs(resolveOs(ua));
        info.setDeviceType(resolveDeviceType(ua));
        return info;
    }

    private static String resolveBrowser(String ua) {
        if (ua.contains("edg/")) {
            return "Edge";
        }
        if (ua.contains("chrome/")) {
            return "Chrome";
        }
        if (ua.contains("firefox/")) {
            return "Firefox";
        }
        if (ua.contains("safari/")) {
            return "Safari";
        }
        if (ua.contains("msie") || ua.contains("trident/")) {
            return "IE";
        }
        return "Unknown";
    }

    private static String resolveOs(String ua) {
        if (ua.contains("windows")) {
            return "Windows";
        }
        if (ua.contains("mac os x")) {
            return "macOS";
        }
        if (ua.contains("android")) {
            return "Android";
        }
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios")) {
            return "iOS";
        }
        if (ua.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }

    private static String resolveDeviceType(String ua) {
        if (ua.contains("ipad") || ua.contains("tablet")) {
            return "Tablet";
        }
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile";
        }
        return "PC";
    }

    public static class UserAgentInfo {
        private String browser = "Unknown";
        private String os = "Unknown";
        private String deviceType = "Unknown";

        public String getBrowser() {
            return browser;
        }

        public void setBrowser(String browser) {
            this.browser = browser;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }
    }
}
