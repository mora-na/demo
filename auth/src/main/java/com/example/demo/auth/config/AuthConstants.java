package com.example.demo.auth.config;

import com.example.demo.common.config.ConfigBinding;
import com.example.demo.common.config.ConfigField;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Auth 模块常量配置，统一维护可覆盖默认值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@ConfigBinding(group = "auth")
@Data
@Component
@ConfigurationProperties(prefix = "auth.constants")
public class AuthConstants {

    private Token token = new Token();
    private Filter filter = new Filter();
    private Captcha captcha = new Captcha();
    private LoginAttempt loginAttempt = new LoginAttempt();
    private Password password = new Password();
    @ConfigField(seed = true, hotUpdate = true)
    private Profile profile = new Profile();
    private LoginLog loginLog = new LoginLog();
    @ConfigField(seed = true, hotUpdate = true)
    private Security security = new Security();
    private Controller controller = new Controller();

    @Data
    public static class Token {
        public static final String DEFAULT_AUTHORIZATION_HEADER = "Authorization";
        public static final String DEFAULT_BEARER_PREFIX = "Bearer ";
        public static final String DEFAULT_TOKEN_TYPE = "Bearer";
        public static final String DEFAULT_JWT_HEADER_ALG_KEY = "alg";
        public static final String DEFAULT_JWT_HEADER_TYPE_KEY = "typ";
        public static final String DEFAULT_JWT_HEADER_ALG_VALUE = "HS256";
        public static final String DEFAULT_JWT_HEADER_TYPE_VALUE = "JWT";
        public static final String DEFAULT_JWT_CLAIM_SUBJECT = "sub";
        public static final String DEFAULT_JWT_CLAIM_USER_ID = "uid";
        public static final String DEFAULT_JWT_CLAIM_ISSUED_AT = "iat";
        public static final String DEFAULT_JWT_CLAIM_EXPIRES_AT = "exp";
        public static final String DEFAULT_JWT_CLAIM_JWT_ID = "jti";
        public static final String DEFAULT_JWT_CLAIM_TOKEN_VERSION = "ver";
        public static final String DEFAULT_SIGN_ALGORITHM = "HmacSHA256";
        public static final String DEFAULT_STORE_KEY_PREFIX = "auth:token:";

        private String authorizationHeader = DEFAULT_AUTHORIZATION_HEADER;
        private String bearerPrefix = DEFAULT_BEARER_PREFIX;
        private String tokenType = DEFAULT_TOKEN_TYPE;
        private String jwtHeaderAlgKey = DEFAULT_JWT_HEADER_ALG_KEY;
        private String jwtHeaderTypeKey = DEFAULT_JWT_HEADER_TYPE_KEY;
        private String jwtHeaderAlgValue = DEFAULT_JWT_HEADER_ALG_VALUE;
        private String jwtHeaderTypeValue = DEFAULT_JWT_HEADER_TYPE_VALUE;
        private String jwtClaimSubject = DEFAULT_JWT_CLAIM_SUBJECT;
        private String jwtClaimUserId = DEFAULT_JWT_CLAIM_USER_ID;
        private String jwtClaimIssuedAt = DEFAULT_JWT_CLAIM_ISSUED_AT;
        private String jwtClaimExpiresAt = DEFAULT_JWT_CLAIM_EXPIRES_AT;
        private String jwtClaimJwtId = DEFAULT_JWT_CLAIM_JWT_ID;
        private String jwtClaimTokenVersion = DEFAULT_JWT_CLAIM_TOKEN_VERSION;
        private String signAlgorithm = DEFAULT_SIGN_ALGORITHM;
        private String storeKeyPrefix = DEFAULT_STORE_KEY_PREFIX;
    }

    @Data
    public static class Filter {
        public static final String DEFAULT_OPTIONS_METHOD = "OPTIONS";
        public static final String DEFAULT_GET_METHOD = "GET";
        public static final String DEFAULT_POST_METHOD = "POST";
        public static final String DEFAULT_PUT_METHOD = "PUT";
        public static final String DEFAULT_TOKEN_MISSING_MESSAGE_KEY = "auth.token.missing";
        public static final String DEFAULT_TOKEN_INVALID_MESSAGE_KEY = "auth.token.invalid";
        public static final String DEFAULT_USER_INVALID_MESSAGE_KEY = "auth.user.invalid";
        public static final String DEFAULT_USER_NOT_FOUND_MESSAGE_KEY = "auth.user.not.found";
        public static final String DEFAULT_USER_DISABLED_MESSAGE_KEY = "auth.user.disabled";
        public static final String DEFAULT_PASSWORD_CHANGE_REQUIRED_MESSAGE_KEY = "auth.password.change.required";
        public static final String DEFAULT_PASSWORD_CHANGE_PROFILE_PATH = "/auth/profile";
        public static final String DEFAULT_PASSWORD_CHANGE_LOGOUT_PATH = "/auth/logout";

        private String optionsMethod = DEFAULT_OPTIONS_METHOD;
        private String getMethod = DEFAULT_GET_METHOD;
        private String postMethod = DEFAULT_POST_METHOD;
        private String putMethod = DEFAULT_PUT_METHOD;
        private String tokenMissingMessageKey = DEFAULT_TOKEN_MISSING_MESSAGE_KEY;
        private String tokenInvalidMessageKey = DEFAULT_TOKEN_INVALID_MESSAGE_KEY;
        private String userInvalidMessageKey = DEFAULT_USER_INVALID_MESSAGE_KEY;
        private String userNotFoundMessageKey = DEFAULT_USER_NOT_FOUND_MESSAGE_KEY;
        private String userDisabledMessageKey = DEFAULT_USER_DISABLED_MESSAGE_KEY;
        private String passwordChangeRequiredMessageKey = DEFAULT_PASSWORD_CHANGE_REQUIRED_MESSAGE_KEY;
        private String passwordChangeProfilePath = DEFAULT_PASSWORD_CHANGE_PROFILE_PATH;
        private String passwordChangeLogoutPath = DEFAULT_PASSWORD_CHANGE_LOGOUT_PATH;
    }

    @Data
    public static class Captcha {
        public static final String DEFAULT_IMAGE_PREFIX = "data:image/png;base64,";
        public static final String DEFAULT_CODE_CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        public static final String DEFAULT_PNG_FORMAT = "png";
        public static final String DEFAULT_FALLBACK_FONT_FAMILY = "SansSerif";
        public static final int DEFAULT_NOISE_DOT_MIN_COUNT = 20;
        public static final int DEFAULT_NOISE_DOT_DENSITY_DIVISOR = 150;
        public static final int DEFAULT_FONT_MIN_SIZE = 18;
        public static final int DEFAULT_FONT_PADDING = 10;
        public static final int DEFAULT_CHAR_COLOR_MIN = 30;
        public static final int DEFAULT_CHAR_COLOR_MAX = 160;
        public static final int DEFAULT_LINE_DOT_COLOR_MIN = 120;
        public static final int DEFAULT_LINE_DOT_COLOR_MAX = 200;
        public static final String DEFAULT_FONT_RESOURCE_CLASSPATH_PREFIX = "classpath:";
        public static final String DEFAULT_STORE_KEY_PREFIX = "auth:captcha:";

        private String imagePrefix = DEFAULT_IMAGE_PREFIX;
        private String codeCharset = DEFAULT_CODE_CHARSET;
        private String pngFormat = DEFAULT_PNG_FORMAT;
        private String fallbackFontFamily = DEFAULT_FALLBACK_FONT_FAMILY;
        private int noiseDotMinCount = DEFAULT_NOISE_DOT_MIN_COUNT;
        private int noiseDotDensityDivisor = DEFAULT_NOISE_DOT_DENSITY_DIVISOR;
        private int fontMinSize = DEFAULT_FONT_MIN_SIZE;
        private int fontPadding = DEFAULT_FONT_PADDING;
        private int charColorMin = DEFAULT_CHAR_COLOR_MIN;
        private int charColorMax = DEFAULT_CHAR_COLOR_MAX;
        private int lineDotColorMin = DEFAULT_LINE_DOT_COLOR_MIN;
        private int lineDotColorMax = DEFAULT_LINE_DOT_COLOR_MAX;
        private String fontResourceClasspathPrefix = DEFAULT_FONT_RESOURCE_CLASSPATH_PREFIX;
        private String storeKeyPrefix = DEFAULT_STORE_KEY_PREFIX;
    }

    @Data
    public static class LoginAttempt {
        public static final String DEFAULT_FAIL_KEY_PREFIX = "auth:login:fail:";
        public static final String DEFAULT_LOCK_KEY_PREFIX = "auth:login:lock:";
        public static final String DEFAULT_MODE_IP = "ip";
        public static final String DEFAULT_MODE_IP_USER = "ip-user";
        public static final String DEFAULT_MODE_USER_IP = "user-ip";
        public static final String DEFAULT_MODE_FALLBACK = "user";

        private String failKeyPrefix = DEFAULT_FAIL_KEY_PREFIX;
        private String lockKeyPrefix = DEFAULT_LOCK_KEY_PREFIX;
        private String modeIp = DEFAULT_MODE_IP;
        private String modeIpUser = DEFAULT_MODE_IP_USER;
        private String modeUserIp = DEFAULT_MODE_USER_IP;
        private String modeFallback = DEFAULT_MODE_FALLBACK;
    }

    @Data
    public static class Password {
        public static final String DEFAULT_MODE_FALLBACK = "plain";
        public static final String DEFAULT_MODE_BCRYPT = "bcrypt";
        public static final String DEFAULT_MODE_SM3 = "sm3";
        public static final String DEFAULT_TRANSPORT_MODE_AES = "aes";
        public static final String DEFAULT_TRANSPORT_MODE_AES_GCM = "aes-gcm";
        public static final String DEFAULT_TRANSPORT_MODE_BASE64 = "base64";
        public static final String DEFAULT_TRANSPORT_MODE_SM2 = "sm2";
        public static final String DEFAULT_TRANSPORT_SPLIT_DELIMITER = ":";
        public static final int DEFAULT_TRANSPORT_SPLIT_LIMIT = 2;
        public static final String DEFAULT_AES_KEY_ALGORITHM = "AES";
        public static final String DEFAULT_AES_TRANSFORMATION = "AES/GCM/NoPadding";
        public static final int DEFAULT_AES_GCM_TAG_LENGTH_BITS = 128;

        private String modeFallback = DEFAULT_MODE_FALLBACK;
        private String modeBcrypt = DEFAULT_MODE_BCRYPT;
        private String modeSm3 = DEFAULT_MODE_SM3;
        private String transportModeAes = DEFAULT_TRANSPORT_MODE_AES;
        private String transportModeAesGcm = DEFAULT_TRANSPORT_MODE_AES_GCM;
        private String transportModeBase64 = DEFAULT_TRANSPORT_MODE_BASE64;
        private String transportModeSm2 = DEFAULT_TRANSPORT_MODE_SM2;
        private String transportSplitDelimiter = DEFAULT_TRANSPORT_SPLIT_DELIMITER;
        private int transportSplitLimit = DEFAULT_TRANSPORT_SPLIT_LIMIT;
        private String aesKeyAlgorithm = DEFAULT_AES_KEY_ALGORITHM;
        private String aesTransformation = DEFAULT_AES_TRANSFORMATION;
        private int aesGcmTagLengthBits = DEFAULT_AES_GCM_TAG_LENGTH_BITS;
    }

    @Data
    public static class Profile {
        public static final int DEFAULT_NEW_PASSWORD_MIN_LENGTH = 6;
        public static final String DEFAULT_USER_AGENT_HEADER = "User-Agent";

        private int newPasswordMinLength = DEFAULT_NEW_PASSWORD_MIN_LENGTH;
        private String userAgentHeader = DEFAULT_USER_AGENT_HEADER;
    }

    @Data
    public static class LoginLog {
        public static final int DEFAULT_TYPE_LOGIN = 1;
        public static final int DEFAULT_TYPE_LOGOUT = 2;
        public static final int DEFAULT_STATUS_FAIL = 0;
        public static final int DEFAULT_STATUS_SUCCESS = 1;

        private int typeLogin = DEFAULT_TYPE_LOGIN;
        private int typeLogout = DEFAULT_TYPE_LOGOUT;
        private int statusFail = DEFAULT_STATUS_FAIL;
        private int statusSuccess = DEFAULT_STATUS_SUCCESS;
    }

    @Data
    public static class Security {

        private LoginAnomaly loginAnomaly = new LoginAnomaly();
        private OperationConfirm operationConfirm = new OperationConfirm();

        @Data
        public static class LoginAnomaly {
            public static final String DEFAULT_UNKNOWN_VALUE = "-";
            public static final String DEFAULT_DEVICE_SEPARATOR = " | ";
            public static final String DEFAULT_MAIL_SEND_FAILED_LOG_TEMPLATE = "send login anomaly mail failed, userId={}, email={}";

            private String unknownValue = DEFAULT_UNKNOWN_VALUE;
            private String deviceSeparator = DEFAULT_DEVICE_SEPARATOR;
            private String mailSendFailedLogTemplate = DEFAULT_MAIL_SEND_FAILED_LOG_TEMPLATE;
        }

        @Data
        public static class OperationConfirm {
            public static final String DEFAULT_CODE_KEY_PREFIX = "auth:operation:confirm:code:";
            public static final String DEFAULT_ATTEMPT_KEY_PREFIX = "auth:operation:confirm:attempt:";
            public static final String DEFAULT_COOLDOWN_KEY_PREFIX = "auth:operation:confirm:cooldown:";
            public static final String DEFAULT_TICKET_KEY_PREFIX = "auth:operation:confirm:ticket:";
            public static final String DEFAULT_KEY_SEPARATOR = ":";
            public static final String DEFAULT_ACTION_KEY_REGEX = "^[A-Za-z0-9:_-]{2,64}$";
            public static final String DEFAULT_CODE_DIGITS = "0123456789";

            private String codeKeyPrefix = DEFAULT_CODE_KEY_PREFIX;
            private String attemptKeyPrefix = DEFAULT_ATTEMPT_KEY_PREFIX;
            private String cooldownKeyPrefix = DEFAULT_COOLDOWN_KEY_PREFIX;
            private String ticketKeyPrefix = DEFAULT_TICKET_KEY_PREFIX;
            private String keySeparator = DEFAULT_KEY_SEPARATOR;
            private String actionKeyRegex = DEFAULT_ACTION_KEY_REGEX;
            private String codeDigits = DEFAULT_CODE_DIGITS;
        }
    }

    @Data
    public static class Controller {
        public static final int DEFAULT_BAD_REQUEST_CODE = 400;
        public static final int DEFAULT_UNAUTHORIZED_CODE = 401;
        public static final int DEFAULT_FORBIDDEN_CODE = 403;
        public static final int DEFAULT_NOT_FOUND_CODE = 404;
        public static final int DEFAULT_TOO_MANY_REQUESTS_CODE = 429;
        public static final int DEFAULT_INTERNAL_SERVER_ERROR_CODE = 500;

        private int badRequestCode = DEFAULT_BAD_REQUEST_CODE;
        private int unauthorizedCode = DEFAULT_UNAUTHORIZED_CODE;
        private int forbiddenCode = DEFAULT_FORBIDDEN_CODE;
        private int notFoundCode = DEFAULT_NOT_FOUND_CODE;
        private int tooManyRequestsCode = DEFAULT_TOO_MANY_REQUESTS_CODE;
        private int internalServerErrorCode = DEFAULT_INTERNAL_SERVER_ERROR_CODE;
    }
}
