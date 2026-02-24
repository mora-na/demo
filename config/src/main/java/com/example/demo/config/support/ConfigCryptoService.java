package com.example.demo.config.support;

import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 配置加解密服务。
 */
@Component
public class ConfigCryptoService {

    private static final String ENC_PREFIX = "ENC(";
    private static final String ENC_SUFFIX = ")";

    private final StringEncryptor encryptor;

    public ConfigCryptoService(@Qualifier("jasyptStringEncryptor") StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public String encryptIfNeeded(boolean sensitive, String raw) {
        if (!sensitive || StringUtils.isBlank(raw)) {
            return raw;
        }
        if (isEncrypted(raw)) {
            return raw;
        }
        String encrypted = encryptor.encrypt(raw);
        return ENC_PREFIX + encrypted + ENC_SUFFIX;
    }

    public String decryptIfNeeded(boolean sensitive, String cipher) {
        if (!sensitive || StringUtils.isBlank(cipher)) {
            return cipher;
        }
        if (isEncrypted(cipher)) {
            String inner = cipher.substring(ENC_PREFIX.length(), cipher.length() - ENC_SUFFIX.length());
            return encryptor.decrypt(inner);
        }
        return encryptor.decrypt(cipher);
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENC_PREFIX) && value.endsWith(ENC_SUFFIX);
    }
}
