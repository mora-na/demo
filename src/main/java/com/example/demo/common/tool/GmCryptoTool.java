package com.example.demo.common.tool;

import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.engines.Zuc128Engine;
import org.bouncycastle.crypto.engines.Zuc256Engine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 国密加解密工具，提供 SM2/SM3/SM4/ZUC 能力，并预留 SM9 适配器。
 */
public final class GmCryptoTool {

    private static final String PROVIDER = "BC";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private GmCryptoTool() {
    }

    /**
     * 计算 SM3 哈希并返回十六进制字符串。
     *
     * @param value 明文
     * @return SM3 哈希（十六进制）
     */
    public static String sm3Hex(String value) {
        if (value == null) {
            return null;
        }
        return Hex.toHexString(sm3Digest(value.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 计算 SM3 哈希并返回十六进制字符串。
     *
     * @param data 原始字节
     * @return SM3 哈希（十六进制）
     */
    public static String sm3Hex(byte[] data) {
        if (data == null) {
            return null;
        }
        return Hex.toHexString(sm3Digest(data));
    }

    /**
     * 计算 SM3 哈希并返回字节数组。
     *
     * @param value 明文
     * @return SM3 哈希字节
     */
    public static byte[] sm3Digest(String value) {
        if (value == null) {
            return null;
        }
        return sm3Digest(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算 SM3 哈希并返回字节数组。
     *
     * @param data 原始字节
     * @return SM3 哈希字节
     */
    public static byte[] sm3Digest(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SM3", PROVIDER);
            return md.digest(data);
        } catch (Exception e) {
            throw new IllegalStateException("SM3 hash error", e);
        }
    }

    /**
     * 使用 SM2 公钥加密并返回 Base64 密文（C1C3C2）。
     *
     * @param plainText       明文
     * @param base64PublicKey Base64 编码的 X.509 公钥
     * @return Base64 密文
     */
    public static String sm2EncryptBase64(String plainText, String base64PublicKey) {
        if (plainText == null || base64PublicKey == null) {
            return null;
        }
        try {
            byte[] input = plainText.getBytes(StandardCharsets.UTF_8);
            PublicKey publicKey = parseSm2PublicKey(base64PublicKey);
            AsymmetricKeyParameter keyParameter = ECUtil.generatePublicKeyParameter(publicKey);
            SM2Engine engine = new SM2Engine(SM2Engine.Mode.C1C3C2);
            engine.init(true, new ParametersWithRandom(keyParameter, new SecureRandom()));
            byte[] cipher = engine.processBlock(input, 0, input.length);
            return BASE64_ENCODER.encodeToString(cipher);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 encryption error", e);
        }
    }

    /**
     * 解密 SM2 Base64 密文（优先 C1C3C2，不支持则尝试 C1C2C3）。
     *
     * @param base64CipherText Base64 密文
     * @param base64PrivateKey Base64 编码的 PKCS8 私钥
     * @return 明文
     */
    public static String sm2DecryptBase64(String base64CipherText, String base64PrivateKey) {
        if (base64CipherText == null || base64PrivateKey == null) {
            return null;
        }
        try {
            byte[] cipherBytes = BASE64_DECODER.decode(base64CipherText);
            PrivateKey privateKey = parseSm2PrivateKey(base64PrivateKey);
            AsymmetricKeyParameter keyParameter;
            try {
                keyParameter = ECUtil.generatePrivateKeyParameter(privateKey);
            } catch (Exception e) {
                throw new IllegalStateException("SM2 private key parameter error", e);
            }
            byte[] plain = sm2Decrypt(cipherBytes, keyParameter, SM2Engine.Mode.C1C3C2);
            if (plain == null) {
                plain = sm2Decrypt(cipherBytes, keyParameter, SM2Engine.Mode.C1C2C3);
            }
            if (plain == null) {
                throw new IllegalStateException("SM2 decryption error");
            }
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("SM2 cipher text is not valid Base64", e);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 decryption error", e);
        }
    }

    private static byte[] sm2Decrypt(byte[] cipherBytes, AsymmetricKeyParameter privateKey, SM2Engine.Mode mode) {
        try {
            SM2Engine engine = new SM2Engine(mode);
            engine.init(false, privateKey);
            return engine.processBlock(cipherBytes, 0, cipherBytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    private static PublicKey parseSm2PublicKey(String base64PublicKey) {
        try {
            byte[] keyBytes = BASE64_DECODER.decode(base64PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", PROVIDER);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 public key parse error", e);
        }
    }

    private static PrivateKey parseSm2PrivateKey(String base64PrivateKey) {
        try {
            byte[] keyBytes = BASE64_DECODER.decode(base64PrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", PROVIDER);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 private key parse error", e);
        }
    }

    /**
     * 使用 SM4（ECB/PKCS5Padding）加密并返回 Base64 密文。
     *
     * @param plainText 明文
     * @param base64Key Base64 编码的 16 字节密钥
     * @return Base64 密文
     */
    public static String sm4EncryptEcbBase64(String plainText, String base64Key) {
        if (plainText == null || base64Key == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "SM4 key");
        ensureLength(keyBytes, 16, "SM4 key");
        try {
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", PROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return BASE64_ENCODER.encodeToString(cipherBytes);
        } catch (Exception e) {
            throw new IllegalStateException("SM4 encryption error", e);
        }
    }

    /**
     * 解密 SM4（ECB/PKCS5Padding）Base64 密文。
     *
     * @param base64CipherText Base64 密文
     * @param base64Key        Base64 编码的 16 字节密钥
     * @return 明文
     */
    public static String sm4DecryptEcbBase64(String base64CipherText, String base64Key) {
        if (base64CipherText == null || base64Key == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "SM4 key");
        ensureLength(keyBytes, 16, "SM4 key");
        byte[] cipherBytes = decodeBase64(base64CipherText, "SM4 cipher text");
        try {
            Cipher cipher = Cipher.getInstance("SM4/ECB/PKCS5Padding", PROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("SM4 decryption error", e);
        }
    }

    /**
     * 使用 SM4（CBC/PKCS5Padding）加密并返回 Base64 密文。
     *
     * @param plainText 明文
     * @param base64Key Base64 编码的 16 字节密钥
     * @param base64Iv  Base64 编码的 16 字节 IV
     * @return Base64 密文
     */
    public static String sm4EncryptCbcBase64(String plainText, String base64Key, String base64Iv) {
        if (plainText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "SM4 key");
        byte[] ivBytes = decodeBase64(base64Iv, "SM4 IV");
        ensureLength(keyBytes, 16, "SM4 key");
        ensureLength(ivBytes, 16, "SM4 IV");
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", PROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return BASE64_ENCODER.encodeToString(cipherBytes);
        } catch (Exception e) {
            throw new IllegalStateException("SM4 encryption error", e);
        }
    }

    /**
     * 解密 SM4（CBC/PKCS5Padding）Base64 密文。
     *
     * @param base64CipherText Base64 密文
     * @param base64Key        Base64 编码的 16 字节密钥
     * @param base64Iv         Base64 编码的 16 字节 IV
     * @return 明文
     */
    public static String sm4DecryptCbcBase64(String base64CipherText, String base64Key, String base64Iv) {
        if (base64CipherText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "SM4 key");
        byte[] ivBytes = decodeBase64(base64Iv, "SM4 IV");
        ensureLength(keyBytes, 16, "SM4 key");
        ensureLength(ivBytes, 16, "SM4 IV");
        byte[] cipherBytes = decodeBase64(base64CipherText, "SM4 cipher text");
        try {
            Cipher cipher = Cipher.getInstance("SM4/CBC/PKCS5Padding", PROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(ivBytes));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("SM4 decryption error", e);
        }
    }

    /**
     * 使用 ZUC-128 流加密并返回 Base64 密文。
     *
     * @param plainText 明文
     * @param base64Key Base64 编码的 16 字节密钥
     * @param base64Iv  Base64 编码的 16 字节 IV
     * @return Base64 密文
     */
    public static String zucEncryptBase64(String plainText, String base64Key, String base64Iv) {
        if (plainText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "ZUC key");
        byte[] ivBytes = decodeBase64(base64Iv, "ZUC IV");
        ensureLength(keyBytes, 16, "ZUC-128 key");
        ensureLength(ivBytes, 16, "ZUC-128 IV");
        byte[] cipherBytes = zuc128Process(plainText.getBytes(StandardCharsets.UTF_8), keyBytes, ivBytes);
        return BASE64_ENCODER.encodeToString(cipherBytes);
    }

    /**
     * 解密 ZUC-128 Base64 密文。
     *
     * @param base64CipherText Base64 密文
     * @param base64Key        Base64 编码的 16 字节密钥
     * @param base64Iv         Base64 编码的 16 字节 IV
     * @return 明文
     */
    public static String zucDecryptBase64(String base64CipherText, String base64Key, String base64Iv) {
        if (base64CipherText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "ZUC key");
        byte[] ivBytes = decodeBase64(base64Iv, "ZUC IV");
        ensureLength(keyBytes, 16, "ZUC-128 key");
        ensureLength(ivBytes, 16, "ZUC-128 IV");
        byte[] cipherBytes = decodeBase64(base64CipherText, "ZUC cipher text");
        byte[] plainBytes = zuc128Process(cipherBytes, keyBytes, ivBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    /**
     * SM9 适配器接口，用于接入具体实现。
     */
    public interface Sm9Adapter {
        String encryptBase64(String plainText, String base64PublicKey);

        String decryptBase64(String base64CipherText, String base64PrivateKey);
    }

    private static volatile Sm9Adapter sm9Adapter;

    /**
     * 注册 SM9 适配器实现。
     *
     * @param adapter 适配器实现
     */
    public static void registerSm9Adapter(Sm9Adapter adapter) {
        sm9Adapter = adapter;
    }

    /**
     * 通过已注册的 SM9 适配器加密。
     *
     * @param plainText       明文
     * @param base64PublicKey Base64 公钥
     * @return Base64 密文
     */
    public static String sm9EncryptBase64(String plainText, String base64PublicKey) {
        Sm9Adapter adapter = sm9Adapter;
        if (adapter == null) {
            throw new IllegalStateException("SM9 adapter is not registered");
        }
        return adapter.encryptBase64(plainText, base64PublicKey);
    }

    /**
     * 通过已注册的 SM9 适配器解密。
     *
     * @param base64CipherText Base64 密文
     * @param base64PrivateKey Base64 私钥
     * @return 明文
     */
    public static String sm9DecryptBase64(String base64CipherText, String base64PrivateKey) {
        Sm9Adapter adapter = sm9Adapter;
        if (adapter == null) {
            throw new IllegalStateException("SM9 adapter is not registered");
        }
        return adapter.decryptBase64(base64CipherText, base64PrivateKey);
    }

    /**
     * 使用 SM4（GCM/NoPadding）加密并返回 Base64 密文。
     *
     * @param plainText  明文
     * @param base64Key  Base64 编码的 16 字节密钥
     * @param base64Iv   Base64 编码的 IV（建议 12 字节）
     * @param base64Aad  Base64 编码的 AAD（可为 null）
     * @return Base64 密文
     */
    public static String sm4EncryptGcmBase64(String plainText, String base64Key, String base64Iv, String base64Aad) {
        if (plainText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "SM4 key");
        byte[] ivBytes = decodeBase64(base64Iv, "SM4 IV");
        ensureLength(keyBytes, 16, "SM4 key");
        ensureMinLength(ivBytes, 12, "SM4 IV");
        byte[] aadBytes = base64Aad == null ? null : decodeBase64(base64Aad, "SM4 AAD");
        try {
            Cipher cipher = Cipher.getInstance("SM4/GCM/NoPadding", PROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            GCMParameterSpec spec = new GCMParameterSpec(128, ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            if (aadBytes != null && aadBytes.length > 0) {
                cipher.updateAAD(aadBytes);
            }
            byte[] cipherBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return BASE64_ENCODER.encodeToString(cipherBytes);
        } catch (Exception e) {
            throw new IllegalStateException("SM4 GCM encryption error", e);
        }
    }

    /**
     * 解密 SM4（GCM/NoPadding）Base64 密文。
     *
     * @param base64CipherText Base64 密文
     * @param base64Key        Base64 编码的 16 字节密钥
     * @param base64Iv         Base64 编码的 IV（建议 12 字节）
     * @param base64Aad        Base64 编码的 AAD（可为 null）
     * @return 明文
     */
    public static String sm4DecryptGcmBase64(String base64CipherText,
                                             String base64Key,
                                             String base64Iv,
                                             String base64Aad) {
        if (base64CipherText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "SM4 key");
        byte[] ivBytes = decodeBase64(base64Iv, "SM4 IV");
        ensureLength(keyBytes, 16, "SM4 key");
        ensureMinLength(ivBytes, 12, "SM4 IV");
        byte[] aadBytes = base64Aad == null ? null : decodeBase64(base64Aad, "SM4 AAD");
        byte[] cipherBytes = decodeBase64(base64CipherText, "SM4 cipher text");
        try {
            Cipher cipher = Cipher.getInstance("SM4/GCM/NoPadding", PROVIDER);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "SM4");
            GCMParameterSpec spec = new GCMParameterSpec(128, ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            if (aadBytes != null && aadBytes.length > 0) {
                cipher.updateAAD(aadBytes);
            }
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("SM4 GCM decryption error", e);
        }
    }

    /**
     * 使用 ZUC-256 流加密并返回 Base64 密文。
     *
     * @param plainText 明文
     * @param base64Key Base64 编码的 32 字节密钥
     * @param base64Iv  Base64 编码的 25 字节 IV
     * @return Base64 密文
     */
    public static String zuc256EncryptBase64(String plainText, String base64Key, String base64Iv) {
        if (plainText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "ZUC-256 key");
        byte[] ivBytes = decodeBase64(base64Iv, "ZUC-256 IV");
        ensureLength(keyBytes, 32, "ZUC-256 key");
        ensureLength(ivBytes, 25, "ZUC-256 IV");
        byte[] cipherBytes = zuc256Process(plainText.getBytes(StandardCharsets.UTF_8), keyBytes, ivBytes);
        return BASE64_ENCODER.encodeToString(cipherBytes);
    }

    /**
     * 解密 ZUC-256 Base64 密文。
     *
     * @param base64CipherText Base64 密文
     * @param base64Key        Base64 编码的 32 字节密钥
     * @param base64Iv         Base64 编码的 25 字节 IV
     * @return 明文
     */
    public static String zuc256DecryptBase64(String base64CipherText, String base64Key, String base64Iv) {
        if (base64CipherText == null || base64Key == null || base64Iv == null) {
            return null;
        }
        byte[] keyBytes = decodeBase64(base64Key, "ZUC-256 key");
        byte[] ivBytes = decodeBase64(base64Iv, "ZUC-256 IV");
        ensureLength(keyBytes, 32, "ZUC-256 key");
        ensureLength(ivBytes, 25, "ZUC-256 IV");
        byte[] cipherBytes = decodeBase64(base64CipherText, "ZUC-256 cipher text");
        byte[] plainBytes = zuc256Process(cipherBytes, keyBytes, ivBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }

    private static byte[] zuc128Process(byte[] input, byte[] key, byte[] iv) {
        try {
            Zuc128Engine engine = new Zuc128Engine();
            engine.init(true, new ParametersWithIV(new KeyParameter(key), iv));
            byte[] output = new byte[input.length];
            engine.processBytes(input, 0, input.length, output, 0);
            return output;
        } catch (Exception e) {
            throw new IllegalStateException("ZUC processing error", e);
        }
    }

    private static byte[] zuc256Process(byte[] input, byte[] key, byte[] iv) {
        try {
            Zuc256Engine engine = new Zuc256Engine();
            engine.init(true, new ParametersWithIV(new KeyParameter(key), iv));
            byte[] output = new byte[input.length];
            engine.processBytes(input, 0, input.length, output, 0);
            return output;
        } catch (Exception e) {
            throw new IllegalStateException("ZUC-256 processing error", e);
        }
    }

    private static byte[] decodeBase64(String value, String label) {
        try {
            return BASE64_DECODER.decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(label + " is not valid Base64", e);
        }
    }

    private static void ensureLength(byte[] data, int expected, String label) {
        if (data == null || data.length != expected) {
            throw new IllegalStateException(label + " length must be " + expected + " bytes");
        }
    }

    private static void ensureMinLength(byte[] data, int expected, String label) {
        if (data == null || data.length < expected) {
            throw new IllegalStateException(label + " length must be at least " + expected + " bytes");
        }
    }
}
