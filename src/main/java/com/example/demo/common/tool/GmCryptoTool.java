package com.example.demo.common.tool;

import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * GM crypto helper with SM2 encryption/decryption and SM3 hashing.
 */
public final class GmCryptoTool {

    private static final String PROVIDER = "BC";

    static {
        if (Security.getProvider(PROVIDER) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private GmCryptoTool() {
    }

    /**
     * Calculate SM3 hash and return hex output.
     *
     * @param value plain text
     * @return SM3 hash in hex
     */
    public static String sm3Hex(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SM3", PROVIDER);
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SM3 hash error", e);
        }
    }

    /**
     * Encrypt with SM2 public key and return Base64 cipher text (C1C3C2).
     *
     * @param plainText       plain text
     * @param base64PublicKey Base64-encoded X.509 public key
     * @return Base64 cipher text
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
            return Base64.getEncoder().encodeToString(cipher);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 encryption error", e);
        }
    }

    /**
     * Decrypt SM2 Base64 cipher text (try C1C3C2, then C1C2C3).
     *
     * @param base64CipherText Base64 cipher text
     * @param base64PrivateKey Base64-encoded PKCS8 private key
     * @return plain text
     */
    public static String sm2DecryptBase64(String base64CipherText, String base64PrivateKey) {
        if (base64CipherText == null || base64PrivateKey == null) {
            return null;
        }
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(base64CipherText);
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
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", PROVIDER);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 public key parse error", e);
        }
    }

    private static PrivateKey parseSm2PrivateKey(String base64PrivateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", PROVIDER);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("SM2 private key parse error", e);
        }
    }
}
