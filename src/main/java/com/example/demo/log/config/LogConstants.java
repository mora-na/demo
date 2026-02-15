package com.example.demo.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Log 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Data
@Component
@ConfigurationProperties(prefix = "log.constants")
public class LogConstants {

    private Controller controller = new Controller();
    private Message message = new Message();
    private Page page = new Page();
    private Query query = new Query();
    private Status status = new Status();
    private Http http = new Http();
    private Aspect aspect = new Aspect();
    private Ip ip = new Ip();
    private UserAgent userAgent = new UserAgent();

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        /**
         * 参数非法场景错误码。
         */
        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        /**
         * 资源不存在场景错误码。
         */
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        /**
         * 服务执行失败场景错误码。
         */
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }

    @Data
    public static class Message {
        public static final String DEFAULT_COMMON_DELETE_FAILED = "common.delete.failed";
        public static final String DEFAULT_LOGIN_LOG_PERSIST_FAILED = "登录日志入库失败";
        public static final String DEFAULT_OPER_LOG_PERSIST_FAILED = "操作日志入库失败";
        public static final String DEFAULT_SPEL_PARSE_FAILED = "解析操作日志SpEL失败: {}";

        /**
         * 通用删除失败 i18n 消息键。
         */
        private String commonDeleteFailed = DEFAULT_COMMON_DELETE_FAILED;
        /**
         * 登录日志异步入库失败日志模板。
         */
        private String loginLogPersistFailed = DEFAULT_LOGIN_LOG_PERSIST_FAILED;
        /**
         * 操作日志异步入库失败日志模板。
         */
        private String operLogPersistFailed = DEFAULT_OPER_LOG_PERSIST_FAILED;
        /**
         * SpEL 解析失败日志模板（第一个占位符为原模板字符串）。
         */
        private String spelParseFailed = DEFAULT_SPEL_PARSE_FAILED;
    }

    @Data
    public static class Page {
        public static final long DEFAULT_PAGE_NUM = 1L;
        public static final long DEFAULT_PAGE_SIZE = 10L;

        /**
         * 分页查询对象为空时默认页码。
         */
        private long defaultPageNum = DEFAULT_PAGE_NUM;
        /**
         * 分页查询对象为空时默认页大小。
         */
        private long defaultPageSize = DEFAULT_PAGE_SIZE;
    }

    @Data
    public static class Query {
        public static final String DEFAULT_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

        /**
         * 查询时间字符串的默认解析格式。
         */
        private String dateTimePattern = DEFAULT_DATE_TIME_PATTERN;
    }

    @Data
    public static class Status {
        public static final int DEFAULT_OPER_SUCCESS = 1;
        public static final int DEFAULT_OPER_FAILED = 0;

        /**
         * 操作日志成功状态值。
         */
        private int operSuccess = DEFAULT_OPER_SUCCESS;
        /**
         * 操作日志失败状态值。
         */
        private int operFailed = DEFAULT_OPER_FAILED;
    }

    @Data
    public static class Http {
        public static final String DEFAULT_GET_METHOD = "GET";
        public static final String DEFAULT_OPTIONS_METHOD = "OPTIONS";
        public static final String DEFAULT_POST_METHOD = "POST";
        public static final String DEFAULT_PUT_METHOD = "PUT";
        public static final String DEFAULT_PATCH_METHOD = "PATCH";
        public static final String DEFAULT_DELETE_METHOD = "DELETE";
        public static final String DEFAULT_PERMISSION_SEPARATOR = ":";
        public static final String DEFAULT_METHOD_URL_SEPARATOR = " ";

        /**
         * GET 方法名。
         */
        private String getMethod = DEFAULT_GET_METHOD;
        /**
         * OPTIONS 方法名。
         */
        private String optionsMethod = DEFAULT_OPTIONS_METHOD;
        /**
         * POST 方法名。
         */
        private String postMethod = DEFAULT_POST_METHOD;
        /**
         * PUT 方法名。
         */
        private String putMethod = DEFAULT_PUT_METHOD;
        /**
         * PATCH 方法名。
         */
        private String patchMethod = DEFAULT_PATCH_METHOD;
        /**
         * DELETE 方法名。
         */
        private String deleteMethod = DEFAULT_DELETE_METHOD;
        /**
         * 权限字符串分隔符，用于 `module:action` 取模块前缀。
         */
        private String permissionSeparator = DEFAULT_PERMISSION_SEPARATOR;
        /**
         * HTTP 方法与 URL 拼接分隔符。
         */
        private String methodUrlSeparator = DEFAULT_METHOD_URL_SEPARATOR;
    }

    @Data
    public static class Aspect {
        public static final int DEFAULT_MAX_TEXT_LENGTH = 2000;
        public static final String DEFAULT_SPEL_PATTERN = "#\\{(.+?)}";
        public static final String DEFAULT_SPEL_NULL_LITERAL = "null";
        public static final String DEFAULT_MASK_VALUE = "******";
        public static final String DEFAULT_SPRING_VALIDATION_PACKAGE_PREFIX = "org.springframework.validation.";
        public static final String DEFAULT_SPRING_MULTIPART_PACKAGE_PREFIX = "org.springframework.web.multipart.";

        /**
         * 操作参数/结果/错误文本统一截断长度。
         */
        private int maxTextLength = DEFAULT_MAX_TEXT_LENGTH;
        /**
         * 默认脱敏字段列表（当注解未显式声明时使用）。
         */
        private List<String> defaultExcludeParams = new ArrayList<>(
                Arrays.asList("password", "oldPassword", "newPassword", "token"));
        /**
         * 基于权限前缀自动推导 title 的映射表。
         */
        private Map<String, String> titleMappings = defaultTitleMappings();
        /**
         * SpEL 模板占位符正则。
         */
        private String spelPattern = DEFAULT_SPEL_PATTERN;
        /**
         * SpEL 表达式值为 null 时替换文本。
         */
        private String spelNullLiteral = DEFAULT_SPEL_NULL_LITERAL;
        /**
         * 脱敏替换值。
         */
        private String maskValue = DEFAULT_MASK_VALUE;
        /**
         * 参数过滤时忽略的 Spring Validation 包名前缀。
         */
        private String springValidationPackagePrefix = DEFAULT_SPRING_VALIDATION_PACKAGE_PREFIX;
        /**
         * 参数过滤时忽略的 Spring Multipart 包名前缀。
         */
        private String springMultipartPackagePrefix = DEFAULT_SPRING_MULTIPART_PACKAGE_PREFIX;

        private static Map<String, String> defaultTitleMappings() {
            Map<String, String> mappings = new LinkedHashMap<>();
            mappings.put("user", "用户管理");
            mappings.put("role", "角色管理");
            mappings.put("menu", "菜单管理");
            mappings.put("dept", "部门管理");
            mappings.put("post", "岗位管理");
            mappings.put("permission", "权限管理");
            mappings.put("notice", "系统通知");
            mappings.put("job", "定时任务");
            mappings.put("order", "订单管理");
            mappings.put("data-scope", "数据权限");
            mappings.put("log", "操作日志");
            mappings.put("login-log", "登录日志");
            return mappings;
        }
    }

    @Data
    public static class Ip {
        public static final String DEFAULT_UNKNOWN_TOKEN = "unknown";
        public static final String DEFAULT_MULTI_IP_SEPARATOR = ",";
        public static final String DEFAULT_INTERNAL_IP_TEXT = "内网IP";
        public static final String DEFAULT_UNKNOWN_LOCATION_TEXT = "未知";
        public static final String DEFAULT_IPV4_SEGMENT_SEPARATOR_REGEX = "\\.";
        public static final String DEFAULT_IPV4_LOOPBACK_PREFIX = "127.";
        public static final String DEFAULT_IPV6_LOOPBACK_FULL = "0:0:0:0:0:0:0:1";
        public static final String DEFAULT_IPV6_LOOPBACK_SHORT = "::1";
        public static final String DEFAULT_PRIVATE_A_PREFIX = "10.";
        public static final String DEFAULT_PRIVATE_C_PREFIX = "192.168.";
        public static final String DEFAULT_PRIVATE_B_PREFIX = "172.";
        public static final int DEFAULT_PRIVATE_B_SECOND_OCTET_MIN = 16;
        public static final int DEFAULT_PRIVATE_B_SECOND_OCTET_MAX = 31;

        /**
         * 解析客户端 IP 时依次检查的代理头。
         */
        private List<String> headers = new ArrayList<>(Arrays.asList(
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        ));
        /**
         * 代理头中表示无效 IP 的占位值。
         */
        private String unknownToken = DEFAULT_UNKNOWN_TOKEN;
        /**
         * 多级代理 IP 串分隔符。
         */
        private String multiIpSeparator = DEFAULT_MULTI_IP_SEPARATOR;
        /**
         * 内网 IP 位置文本。
         */
        private String internalIpText = DEFAULT_INTERNAL_IP_TEXT;
        /**
         * 未知 IP 位置文本。
         */
        private String unknownLocationText = DEFAULT_UNKNOWN_LOCATION_TEXT;
        /**
         * IPv4 分段分隔正则。
         */
        private String ipv4SegmentSeparatorRegex = DEFAULT_IPV4_SEGMENT_SEPARATOR_REGEX;
        /**
         * IPv4 本地回环前缀。
         */
        private String ipv4LoopbackPrefix = DEFAULT_IPV4_LOOPBACK_PREFIX;
        /**
         * IPv6 完整写法回环地址。
         */
        private String ipv6LoopbackFull = DEFAULT_IPV6_LOOPBACK_FULL;
        /**
         * IPv6 简写回环地址。
         */
        private String ipv6LoopbackShort = DEFAULT_IPV6_LOOPBACK_SHORT;
        /**
         * A 类私网前缀。
         */
        private String privateAPrefix = DEFAULT_PRIVATE_A_PREFIX;
        /**
         * C 类私网前缀。
         */
        private String privateCPrefix = DEFAULT_PRIVATE_C_PREFIX;
        /**
         * B 类私网前缀。
         */
        private String privateBPrefix = DEFAULT_PRIVATE_B_PREFIX;
        /**
         * B 类私网第二段最小值。
         */
        private int privateBSecondOctetMin = DEFAULT_PRIVATE_B_SECOND_OCTET_MIN;
        /**
         * B 类私网第二段最大值。
         */
        private int privateBSecondOctetMax = DEFAULT_PRIVATE_B_SECOND_OCTET_MAX;
    }

    @Data
    public static class UserAgent {
        public static final String DEFAULT_UNKNOWN = "Unknown";
        public static final String DEFAULT_PC = "PC";

        public static final String DEFAULT_BROWSER_EDGE_TOKEN = "edg/";
        public static final String DEFAULT_BROWSER_CHROME_TOKEN = "chrome/";
        public static final String DEFAULT_BROWSER_FIREFOX_TOKEN = "firefox/";
        public static final String DEFAULT_BROWSER_SAFARI_TOKEN = "safari/";
        public static final String DEFAULT_BROWSER_IE_TOKEN = "msie";
        public static final String DEFAULT_BROWSER_TRIDENT_TOKEN = "trident/";

        public static final String DEFAULT_BROWSER_EDGE_NAME = "Edge";
        public static final String DEFAULT_BROWSER_CHROME_NAME = "Chrome";
        public static final String DEFAULT_BROWSER_FIREFOX_NAME = "Firefox";
        public static final String DEFAULT_BROWSER_SAFARI_NAME = "Safari";
        public static final String DEFAULT_BROWSER_IE_NAME = "IE";

        public static final String DEFAULT_OS_WINDOWS_TOKEN = "windows";
        public static final String DEFAULT_OS_MAC_TOKEN = "mac os x";
        public static final String DEFAULT_OS_ANDROID_TOKEN = "android";
        public static final String DEFAULT_OS_IPHONE_TOKEN = "iphone";
        public static final String DEFAULT_OS_IPAD_TOKEN = "ipad";
        public static final String DEFAULT_OS_IOS_TOKEN = "ios";
        public static final String DEFAULT_OS_LINUX_TOKEN = "linux";

        public static final String DEFAULT_OS_WINDOWS_NAME = "Windows";
        public static final String DEFAULT_OS_MAC_NAME = "macOS";
        public static final String DEFAULT_OS_ANDROID_NAME = "Android";
        public static final String DEFAULT_OS_IOS_NAME = "iOS";
        public static final String DEFAULT_OS_LINUX_NAME = "Linux";

        public static final String DEFAULT_DEVICE_TABLET_TOKEN = "tablet";
        public static final String DEFAULT_DEVICE_MOBILE_TOKEN = "mobile";
        public static final String DEFAULT_DEVICE_TABLET_NAME = "Tablet";
        public static final String DEFAULT_DEVICE_MOBILE_NAME = "Mobile";

        /**
         * 无法识别时的默认名称。
         */
        private String unknown = DEFAULT_UNKNOWN;
        /**
         * 桌面设备默认名称。
         */
        private String pc = DEFAULT_PC;

        /**
         * Edge 浏览器关键字。
         */
        private String browserEdgeToken = DEFAULT_BROWSER_EDGE_TOKEN;
        /**
         * Chrome 浏览器关键字。
         */
        private String browserChromeToken = DEFAULT_BROWSER_CHROME_TOKEN;
        /**
         * Firefox 浏览器关键字。
         */
        private String browserFirefoxToken = DEFAULT_BROWSER_FIREFOX_TOKEN;
        /**
         * Safari 浏览器关键字。
         */
        private String browserSafariToken = DEFAULT_BROWSER_SAFARI_TOKEN;
        /**
         * IE 浏览器关键字（旧版）。
         */
        private String browserIeToken = DEFAULT_BROWSER_IE_TOKEN;
        /**
         * IE 浏览器关键字（Trident）。
         */
        private String browserTridentToken = DEFAULT_BROWSER_TRIDENT_TOKEN;

        private String browserEdgeName = DEFAULT_BROWSER_EDGE_NAME;
        private String browserChromeName = DEFAULT_BROWSER_CHROME_NAME;
        private String browserFirefoxName = DEFAULT_BROWSER_FIREFOX_NAME;
        private String browserSafariName = DEFAULT_BROWSER_SAFARI_NAME;
        private String browserIeName = DEFAULT_BROWSER_IE_NAME;

        /**
         * Windows 关键字。
         */
        private String osWindowsToken = DEFAULT_OS_WINDOWS_TOKEN;
        /**
         * macOS 关键字。
         */
        private String osMacToken = DEFAULT_OS_MAC_TOKEN;
        /**
         * Android 关键字。
         */
        private String osAndroidToken = DEFAULT_OS_ANDROID_TOKEN;
        /**
         * iPhone 关键字。
         */
        private String osIphoneToken = DEFAULT_OS_IPHONE_TOKEN;
        /**
         * iPad 关键字。
         */
        private String osIpadToken = DEFAULT_OS_IPAD_TOKEN;
        /**
         * iOS 关键字。
         */
        private String osIosToken = DEFAULT_OS_IOS_TOKEN;
        /**
         * Linux 关键字。
         */
        private String osLinuxToken = DEFAULT_OS_LINUX_TOKEN;

        private String osWindowsName = DEFAULT_OS_WINDOWS_NAME;
        private String osMacName = DEFAULT_OS_MAC_NAME;
        private String osAndroidName = DEFAULT_OS_ANDROID_NAME;
        private String osIosName = DEFAULT_OS_IOS_NAME;
        private String osLinuxName = DEFAULT_OS_LINUX_NAME;

        /**
         * Tablet 关键字。
         */
        private String deviceTabletToken = DEFAULT_DEVICE_TABLET_TOKEN;
        /**
         * Mobile 关键字。
         */
        private String deviceMobileToken = DEFAULT_DEVICE_MOBILE_TOKEN;

        private String deviceTabletName = DEFAULT_DEVICE_TABLET_NAME;
        private String deviceMobileName = DEFAULT_DEVICE_MOBILE_NAME;
    }
}
