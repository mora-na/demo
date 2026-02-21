package com.example.demo.log.support;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.log.config.LogConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * User-Agent 简易解析工具。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public final class UserAgentUtils {

    private static final LogConstants DEFAULTS = new LogConstants();

    private UserAgentUtils() {
    }

    public static UserAgentInfo parse(String userAgent) {
        LogConstants.UserAgent constants = constants().getUserAgent();
        UserAgentInfo info = new UserAgentInfo();
        info.setBrowser(constants.getUnknown());
        info.setOs(constants.getUnknown());
        info.setDeviceType(constants.getUnknown());
        if (StringUtils.isBlank(userAgent)) {
            return info;
        }
        String ua = userAgent.toLowerCase(Locale.ROOT);
        info.setBrowser(resolveBrowser(ua, constants));
        info.setOs(resolveOs(ua, constants));
        info.setDeviceType(resolveDeviceType(ua, constants));
        return info;
    }

    private static String resolveBrowser(String ua, LogConstants.UserAgent constants) {
        if (containsToken(ua, constants.getBrowserEdgeToken())) {
            return StringUtils.defaultIfBlank(constants.getBrowserEdgeName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getBrowserChromeToken())) {
            return StringUtils.defaultIfBlank(constants.getBrowserChromeName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getBrowserFirefoxToken())) {
            return StringUtils.defaultIfBlank(constants.getBrowserFirefoxName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getBrowserSafariToken())) {
            return StringUtils.defaultIfBlank(constants.getBrowserSafariName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getBrowserIeToken())
                || containsToken(ua, constants.getBrowserTridentToken())) {
            return StringUtils.defaultIfBlank(constants.getBrowserIeName(), constants.getUnknown());
        }
        return constants.getUnknown();
    }

    private static String resolveOs(String ua, LogConstants.UserAgent constants) {
        if (containsToken(ua, constants.getOsWindowsToken())) {
            return StringUtils.defaultIfBlank(constants.getOsWindowsName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getOsAndroidToken())) {
            return StringUtils.defaultIfBlank(constants.getOsAndroidName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getOsIphoneToken())
                || containsToken(ua, constants.getOsIpadToken())
                || containsToken(ua, constants.getOsIosToken())) {
            return StringUtils.defaultIfBlank(constants.getOsIosName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getOsMacToken())) {
            return StringUtils.defaultIfBlank(constants.getOsMacName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getOsLinuxToken())) {
            return StringUtils.defaultIfBlank(constants.getOsLinuxName(), constants.getUnknown());
        }
        return constants.getUnknown();
    }

    private static String resolveDeviceType(String ua, LogConstants.UserAgent constants) {
        if (containsToken(ua, constants.getOsIpadToken())
                || containsToken(ua, constants.getDeviceTabletToken())) {
            return StringUtils.defaultIfBlank(constants.getDeviceTabletName(), constants.getUnknown());
        }
        if (containsToken(ua, constants.getDeviceMobileToken())
                || containsToken(ua, constants.getOsAndroidToken())
                || containsToken(ua, constants.getOsIphoneToken())) {
            return StringUtils.defaultIfBlank(constants.getDeviceMobileName(), constants.getUnknown());
        }
        return StringUtils.defaultIfBlank(constants.getPc(), constants.getUnknown());
    }

    private static boolean containsToken(String ua, String token) {
        return StringUtils.isNotBlank(token) && ua.contains(token.toLowerCase(Locale.ROOT));
    }

    private static LogConstants constants() {
        LogConstants bean = SpringContextHolder.getBean(LogConstants.class);
        return bean == null ? DEFAULTS : bean;
    }

    public static class UserAgentInfo {
        private String browser;
        private String os;
        private String deviceType;

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
