package com.example.demo.common.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Base64;

/**
 * Jasypt 加解密实现，采用 SM4/ECB/PKCS5Padding 处理配置密文。
 * 由于 SM4 需要 128 位密钥，使用 SM3 哈希后取前 16 字节作为固定长度密钥。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Component("jasyptEncryptor")
public class JasyptEncryptor implements StringEncryptor {

    private static final String ALGORITHM_NAME = "SM4/ECB/PKCS5Padding";

    static {
        // 注册 BouncyCastle 提供国密算法支持
        Security.addProvider(new BouncyCastleProvider());
    }

    private final SecretKeySpec keySpec;

    /**
     * 构造加解密器并初始化密钥。
     *
     * @param password 原始密钥字符串
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public JasyptEncryptor(@Value("${jasypt.encryptor.password}") String password) {
        byte[] key = normalizeKey(password);
        this.keySpec = new SecretKeySpec(key, "SM4");
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
        try {
            MessageDigest md = MessageDigest.getInstance("SM3", "BC");
            byte[] hash = md.digest(keyStr.getBytes(StandardCharsets.UTF_8));
            byte[] fixed = new byte[16];
            System.arraycopy(hash, 0, fixed, 0, 16);
            return fixed;
        } catch (Exception e) {
            throw new RuntimeException("Key normalization error", e);
        }
    }

    /**
     * 对明文进行 SM4 加密并返回 Base64 字符串。
     *
     * @param message 明文
     * @return 密文（Base64）
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String encrypt(String message) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME, "BC");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("SM4 ECB encryption error", e);
        }
    }

    /**
     * 对 Base64 密文进行 SM4 解密并返回明文。
     *
     * @param encryptedMessage Base64 密文
     * @return 明文
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    @Override
    public String decrypt(String encryptedMessage) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM_NAME, "BC");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encryptedMessage);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("SM4 ECB decryption error", e);
        }
    }

//    public static void main(String[] args) {
//        String password = "xxx";
//        JasyptStringEncryptor jasyptUtil = new JasyptStringEncryptor(password);
//        String encrypt = jasyptUtil.encrypt("xxx");
//        System.out.println(encrypt);
//        String decrypt = jasyptUtil.decrypt(encrypt);
//        System.out.println(decrypt);
//
//    }

}
