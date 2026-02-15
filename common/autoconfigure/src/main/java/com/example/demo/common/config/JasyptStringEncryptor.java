package com.example.demo.common.config;

import com.example.demo.common.tool.GmCryptoTool;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Base64;

/**
 * Jasypt 加解密实现，默认使用 SM4-GCM 加密并兼容历史 SM4-ECB 密文。
 * 由于 SM4 需要 128 位密钥，使用 SM3 哈希后取前 16 字节作为固定长度密钥。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component("jasyptStringEncryptor")
public class JasyptStringEncryptor implements StringEncryptor {

    private static final String GCM_PREFIX = "SM4GCM:";
    private static final int GCM_IV_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    static {
        // 注册 BouncyCastle 提供国密算法支持
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final String base64Key;

    /**
     * 构造加解密器并初始化密钥。
     *
     * @param password 原始密钥字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public JasyptStringEncryptor(@Value("${jasypt.encryptor.password}") String password) {
        if (password == null) {
            throw new IllegalArgumentException("jasypt.encryptor.password is required");
        }
        byte[] key = normalizeKey(password);
        this.base64Key = Base64.getEncoder().encodeToString(key);
    }

    /**
     * 将任意长度密钥归一化为 16 字节 SM4 密钥。
     *
     * @param keyStr 原始密钥字符串
     * @return 16 字节密钥
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    private static byte[] normalizeKey(String keyStr) {
        byte[] hash = GmCryptoTool.sm3Digest(keyStr);
        if (hash == null) {
            throw new IllegalStateException("Key normalization error");
        }
        return Arrays.copyOf(hash, 16);
    }

    /**
     * 对明文进行 SM4-GCM 加密并返回带前缀的 Base64 密文。
     *
     * @param message 明文
     * @return 密文（带前缀）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String encrypt(String message) {
        if (message == null) {
            return null;
        }
        byte[] iv = new byte[GCM_IV_LENGTH];
        SECURE_RANDOM.nextBytes(iv);
        String base64Iv = Base64.getEncoder().encodeToString(iv);
        String cipherText = GmCryptoTool.sm4EncryptGcmBase64(message, base64Key, base64Iv, null);
        return GCM_PREFIX + base64Iv + ":" + cipherText;
    }

    /**
     * 解密 SM4 密文并返回明文（优先 GCM，兼容历史 ECB）。
     *
     * @param encryptedMessage 密文
     * @return 明文
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String decrypt(String encryptedMessage) {
        if (encryptedMessage == null) {
            return null;
        }
        if (encryptedMessage.startsWith(GCM_PREFIX)) {
            String payload = encryptedMessage.substring(GCM_PREFIX.length());
            String[] parts = payload.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalStateException("SM4 GCM cipher text format error");
            }
            return GmCryptoTool.sm4DecryptGcmBase64(parts[1], base64Key, parts[0], null);
        }
        return GmCryptoTool.sm4DecryptEcbBase64(encryptedMessage, base64Key);
    }

//    public static void main(String[] args) {
//        JasyptStringEncryptor jasyptStringEncryptor = new JasyptStringEncryptor("encryptorPassword");
//        String encrypt = jasyptStringEncryptor.encrypt("admin");
//        System.out.println(encrypt);
//        System.out.println(jasyptStringEncryptor.decrypt(encrypt));
//    }

}
