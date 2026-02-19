package com.example.demo.auth.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 认证安全配置校验，避免弱配置导致降级。
 */
@Component
@RequiredArgsConstructor
public class AuthCryptoValidator {

    private final AuthProperties authProperties;
    private final AuthConstants authConstants;

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        validateJwtSecret();
        validateTransportMode();
    }

    private void validateJwtSecret() {
        String secret = authProperties.getJwt() == null ? null : authProperties.getJwt().getSecret();
        if (StringUtils.isBlank(secret)) {
            throw new IllegalStateException("auth.jwt.secret is required");
        }
        int minLength = authProperties.getJwt().getMinSecretLength();
        if (minLength > 0 && secret.length() < minLength) {
            throw new IllegalStateException("auth.jwt.secret length must be >= " + minLength);
        }
        String forbidden = authProperties.getJwt().getForbiddenSecret();
        if (StringUtils.isNotBlank(forbidden) && secret.trim().equals(forbidden)) {
            throw new IllegalStateException("auth.jwt.secret must not use the default value");
        }
    }

    private void validateTransportMode() {
        String mode = authProperties.getPassword() == null ? null : authProperties.getPassword().getTransportMode();
        String normalized = mode == null ? authConstants.getPassword().getModeFallback() : mode.trim().toLowerCase();
        AuthConstants.Password constants = authConstants.getPassword();
        boolean supported = normalized.equalsIgnoreCase(constants.getModeFallback())
                || normalized.equalsIgnoreCase(constants.getTransportModeBase64())
                || normalized.equalsIgnoreCase(constants.getTransportModeAes())
                || normalized.equalsIgnoreCase(constants.getTransportModeAesGcm())
                || normalized.equalsIgnoreCase(constants.getTransportModeSm2());
        if (!supported) {
            throw new IllegalStateException("Unsupported auth.password.transport-mode: " + mode);
        }
    }
}
