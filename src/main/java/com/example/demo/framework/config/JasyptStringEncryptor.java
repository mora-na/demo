package com.example.demo.framework.config;

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
 * @author panzhiwei
 * date   2025/9/3
 * description jasypt工具类 采用SM4算法加解密配置文件
 * （SM4算法需要128位密钥，固定长度限制为16个字符，为了适配任意长度字符密钥，采用SM3哈希算法计算密钥截取前 16 字节作为密钥）
 */
@Component("jasyptStringEncryptor")
public class JasyptStringEncryptor implements StringEncryptor {

    private static final String ALGORITHM_NAME = "SM4/ECB/PKCS5Padding";

    static {
        // 注册 BouncyCastle 提供国密算法支持
        Security.addProvider(new BouncyCastleProvider());
    }

    private final SecretKeySpec keySpec;

    public JasyptStringEncryptor(@Value("${jasypt.encryptor.password}") String password) {
        byte[] key = normalizeKey(password);
        this.keySpec = new SecretKeySpec(key, "SM4");
    }

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

    public static void main(String[] args) {
        String password = "fuck";
        JasyptStringEncryptor jasyptUtil = new JasyptStringEncryptor(password);
        String encrypt = jasyptUtil.encrypt("Unitech@1234");
        System.out.println(encrypt);
        String decrypt = jasyptUtil.decrypt(encrypt);
        System.out.println(decrypt);

    }

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

}
