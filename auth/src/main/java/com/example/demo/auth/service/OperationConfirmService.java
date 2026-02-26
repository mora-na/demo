package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.cache.CacheTool;
import com.example.demo.common.notify.mail.NotifyMailSender;
import com.example.demo.identity.api.dto.IdentityUserDTO;
import com.example.demo.identity.api.facade.IdentityReadFacade;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 敏感操作邮箱二次确认服务。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Service
@RequiredArgsConstructor
public class OperationConfirmService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CacheTool cacheTool;
    private final NotifyMailSender notifyMailSender;
    private final IdentityReadFacade identityReadFacade;
    private final AuthProperties authProperties;
    private final AuthConstants authConstants;

    /**
     * 发送敏感操作确认验证码。
     *
     * @param authUser    当前登录用户
     * @param actionKey   操作标识
     * @param actionLabel 操作描述
     * @return 发送结果
     */
    public SendCodeResult sendCode(AuthUser authUser, String actionKey, String actionLabel) {
        if (!isEnabled()) {
            return SendCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.disabled", 0);
        }
        Long userId = authUser == null ? null : authUser.getId();
        if (userId == null) {
            return SendCodeResult.fail(authConstants.getController().getUnauthorizedCode(),
                    "auth.user.invalid", 0);
        }
        String normalizedActionKey = normalizeActionKey(actionKey);
        if (normalizedActionKey == null) {
            return SendCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.action.invalid", 0);
        }
        IdentityUserDTO user = identityReadFacade.getUserById(userId);
        if (user == null || StringUtils.isBlank(user.getEmail())) {
            return SendCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.email.empty", 0);
        }
        String cooldownKey = buildCooldownKey(userId, normalizedActionKey);
        long retryAfter = cacheTool.getExpire(cooldownKey, java.util.concurrent.TimeUnit.SECONDS);
        if (retryAfter > 0) {
            return SendCodeResult.fail(authConstants.getController().getTooManyRequestsCode(),
                    "auth.operation.confirm.send.too.frequent", retryAfter);
        }
        int codeLength = resolveCodeLength();
        int codeTtlSeconds = resolveCodeTtlSeconds();
        int resendIntervalSeconds = resolveResendIntervalSeconds();
        String code = generateCode(codeLength);
        String subject = resolveMailSubject();
        String content = buildMailContent(user, normalizedActionKey, actionLabel, code, codeTtlSeconds);
        boolean sent = notifyMailSender.sendText(user.getEmail(), subject, content);
        if (!sent) {
            return SendCodeResult.fail(authConstants.getController().getInternalServerErrorCode(),
                    "auth.operation.confirm.send.failed", 0);
        }
        cacheTool.set(buildCodeKey(userId, normalizedActionKey), code, Duration.ofSeconds(codeTtlSeconds));
        cacheTool.delete(buildAttemptKey(userId, normalizedActionKey));
        cacheTool.set(cooldownKey, "1", Duration.ofSeconds(resendIntervalSeconds));
        return SendCodeResult.success();
    }

    /**
     * 校验敏感操作确认验证码，校验成功后签发短期票据。
     *
     * @param authUser  当前登录用户
     * @param actionKey 操作标识
     * @param code      验证码
     * @return 校验结果
     */
    public VerifyCodeResult verifyCode(AuthUser authUser, String actionKey, String code) {
        if (!isEnabled()) {
            return VerifyCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.disabled");
        }
        Long userId = authUser == null ? null : authUser.getId();
        if (userId == null) {
            return VerifyCodeResult.fail(authConstants.getController().getUnauthorizedCode(), "auth.user.invalid");
        }
        String normalizedActionKey = normalizeActionKey(actionKey);
        if (normalizedActionKey == null) {
            return VerifyCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.action.invalid");
        }
        String inputCode = StringUtils.trimToNull(code);
        if (inputCode == null) {
            return VerifyCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.code.invalid");
        }
        String codeKey = buildCodeKey(userId, normalizedActionKey);
        String cachedCode = toStringValue(cacheTool.get(codeKey));
        if (StringUtils.isBlank(cachedCode)) {
            return VerifyCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.code.expired");
        }
        if (!Strings.CS.equals(cachedCode, inputCode)) {
            String attemptKey = buildAttemptKey(userId, normalizedActionKey);
            long attempts = incrementAttempts(attemptKey, codeKey);
            if (attempts >= resolveMaxVerifyAttempts()) {
                cacheTool.delete(codeKey);
                cacheTool.delete(attemptKey);
                cacheTool.delete(buildCooldownKey(userId, normalizedActionKey));
                return VerifyCodeResult.fail(authConstants.getController().getBadRequestCode(),
                        "auth.operation.confirm.code.too-many-attempts");
            }
            return VerifyCodeResult.fail(authConstants.getController().getBadRequestCode(),
                    "auth.operation.confirm.code.invalid");
        }
        cacheTool.delete(codeKey);
        cacheTool.delete(buildAttemptKey(userId, normalizedActionKey));
        int ticketTtlSeconds = resolveTicketTtlSeconds();
        String ticket = UUID.randomUUID().toString().replace("-", "");
        cacheTool.set(buildTicketKey(userId, normalizedActionKey), ticket, Duration.ofSeconds(ticketTtlSeconds));
        long expiresAt = Instant.now().getEpochSecond() + ticketTtlSeconds;
        return VerifyCodeResult.success(ticket, expiresAt);
    }

    /**
     * 消费敏感操作确认票据，匹配成功后立即失效。
     *
     * @param authUser  当前登录用户
     * @param actionKey 操作标识
     * @param ticket    票据
     * @return true 表示校验并消费成功
     */
    public boolean consumeTicket(AuthUser authUser, String actionKey, String ticket) {
        if (!isEnabled()) {
            return true;
        }
        Long userId = authUser == null ? null : authUser.getId();
        if (userId == null) {
            return false;
        }
        String normalizedActionKey = normalizeActionKey(actionKey);
        if (normalizedActionKey == null || StringUtils.isBlank(ticket)) {
            return false;
        }
        String ticketKey = buildTicketKey(userId, normalizedActionKey);
        String cachedTicket = toStringValue(cacheTool.get(ticketKey));
        if (!Strings.CS.equals(cachedTicket, ticket.trim())) {
            return false;
        }
        cacheTool.delete(ticketKey);
        return true;
    }

    private boolean isEnabled() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        return config != null && config.isEnabled();
    }

    private String normalizeActionKey(String actionKey) {
        String normalized = StringUtils.trimToNull(actionKey);
        if (normalized == null) {
            return null;
        }
        String regex = authConstants.getSecurity().getOperationConfirm().getActionKeyRegex();
        if (StringUtils.isBlank(regex)) {
            return normalized;
        }
        try {
            if (!Pattern.matches(regex, normalized)) {
                return null;
            }
        } catch (PatternSyntaxException ex) {
            return null;
        }
        return normalized;
    }

    private int resolveCodeLength() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        int value = config == null ? 6 : config.getCodeLength();
        if (value < 4) {
            return 4;
        }
        if (value > 10) {
            return 10;
        }
        return value;
    }

    private int resolveCodeTtlSeconds() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        int value = config == null ? 300 : config.getCodeTtlSeconds();
        return Math.max(value, 60);
    }

    private int resolveResendIntervalSeconds() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        int value = config == null ? 60 : config.getResendIntervalSeconds();
        return Math.max(value, 10);
    }

    private int resolveTicketTtlSeconds() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        int value = config == null ? 900 : config.getTicketTtlSeconds();
        return Math.max(value, 60);
    }

    private int resolveMaxVerifyAttempts() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        int value = config == null ? 5 : config.getMaxVerifyAttempts();
        return Math.max(value, 1);
    }

    private String resolveMailSubject() {
        AuthProperties.Security.OperationConfirm config = authProperties.getSecurity().getOperationConfirm();
        String subject = config == null ? null : config.getMailSubject();
        return StringUtils.isBlank(subject) ? "敏感操作确认验证码" : subject;
    }

    private String buildMailContent(IdentityUserDTO user,
                                    String actionKey,
                                    String actionLabel,
                                    String code,
                                    int codeTtlSeconds) {
        String displayAction = StringUtils.defaultIfBlank(StringUtils.trimToNull(actionLabel), actionKey);
        String userName = user == null ? "" : StringUtils.defaultString(user.getUserName());
        String builder = "你正在执行敏感操作，需要完成邮箱二次确认。" + '\n' +
                '\n' +
                "账号：" + userName + '\n' +
                "操作：" + displayAction + '\n' +
                "验证码：" + code + '\n' +
                "有效期：" + codeTtlSeconds + " 秒" + '\n' +
                '\n' +
                "若非本人操作，请忽略本邮件并尽快检查账号安全。";
        return builder;
    }

    private long incrementAttempts(String attemptKey, String codeKey) {
        Long attempts = cacheTool.increment(attemptKey);
        long value = attempts == null ? 1L : attempts;
        long remain = cacheTool.getExpire(codeKey, java.util.concurrent.TimeUnit.SECONDS);
        if (remain > 0) {
            cacheTool.expire(attemptKey, Duration.ofSeconds(remain));
        }
        return value;
    }

    private String generateCode(int length) {
        String digits = authConstants.getSecurity().getOperationConfirm().getCodeDigits();
        String source = StringUtils.defaultIfBlank(digits, "0123456789");
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(source.length());
            builder.append(source.charAt(index));
        }
        return builder.toString();
    }

    private String buildCodeKey(Long userId, String actionKey) {
        return buildCompositeKey(authConstants.getSecurity().getOperationConfirm().getCodeKeyPrefix(), userId, actionKey);
    }

    private String buildAttemptKey(Long userId, String actionKey) {
        return buildCompositeKey(authConstants.getSecurity().getOperationConfirm().getAttemptKeyPrefix(), userId, actionKey);
    }

    private String buildCooldownKey(Long userId, String actionKey) {
        return buildCompositeKey(authConstants.getSecurity().getOperationConfirm().getCooldownKeyPrefix(), userId, actionKey);
    }

    private String buildTicketKey(Long userId, String actionKey) {
        return buildCompositeKey(authConstants.getSecurity().getOperationConfirm().getTicketKeyPrefix(), userId, actionKey);
    }

    private String buildCompositeKey(String prefix, Long userId, String actionKey) {
        String basePrefix = StringUtils.defaultIfBlank(prefix, "");
        String separator = authConstants.getSecurity().getOperationConfirm().getKeySeparator();
        String sep = StringUtils.defaultIfBlank(separator, ":");
        return basePrefix + userId + sep + actionKey;
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @Data
    @AllArgsConstructor
    public static class SendCodeResult {
        private boolean success;
        private int code;
        private String messageKey;
        private long retryAfterSeconds;

        public static SendCodeResult success() {
            return new SendCodeResult(true, 200, "auth.operation.confirm.send.success", 0);
        }

        public static SendCodeResult fail(int code, String messageKey, long retryAfterSeconds) {
            return new SendCodeResult(false, code, messageKey, retryAfterSeconds);
        }
    }

    @Data
    @AllArgsConstructor
    public static class VerifyCodeResult {
        private boolean success;
        private int code;
        private String messageKey;
        private String ticket;
        private long expiresAt;

        public static VerifyCodeResult success(String ticket, long expiresAt) {
            return new VerifyCodeResult(true, 200, "auth.operation.confirm.verify.success", ticket, expiresAt);
        }

        public static VerifyCodeResult fail(int code, String messageKey) {
            return new VerifyCodeResult(false, code, messageKey, null, 0);
        }
    }
}
