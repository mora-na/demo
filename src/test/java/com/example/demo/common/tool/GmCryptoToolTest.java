package com.example.demo.common.tool;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GmCryptoToolTest {

    @Test
    void sm3Hex_matchesKnownDigest() {
        String digest = GmCryptoTool.sm3Hex("abc");
        assertEquals("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0", digest);
    }

    @Test
    void sm3Hex_bytesMatchesString() {
        String digestFromString = GmCryptoTool.sm3Hex("abc");
        String digestFromBytes = GmCryptoTool.sm3Hex("abc".getBytes(StandardCharsets.UTF_8));
        assertEquals(digestFromString, digestFromBytes);
    }

    @Test
    void sm2EncryptThenDecrypt_returnsOriginal() throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", "BC");
        generator.initialize(new ECGenParameterSpec("sm2p256v1"));
        KeyPair keyPair = generator.generateKeyPair();
        String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

        String cipher = GmCryptoTool.sm2EncryptBase64("hello", publicKey);
        assertNotNull(cipher);

        String plain = GmCryptoTool.sm2DecryptBase64(cipher, privateKey);
        assertEquals("hello", plain);
    }

    @Test
    void sm4EcbEncryptThenDecrypt_returnsOriginal() {
        String key = base64Of(sequentialBytes(16, 1));
        String cipher = GmCryptoTool.sm4EncryptEcbBase64("hello", key);
        String plain = GmCryptoTool.sm4DecryptEcbBase64(cipher, key);
        assertEquals("hello", plain);
    }

    @Test
    void sm4CbcEncryptThenDecrypt_returnsOriginal() {
        String key = base64Of(sequentialBytes(16, 1));
        String iv = base64Of(sequentialBytes(16, 33));
        String cipher = GmCryptoTool.sm4EncryptCbcBase64("hello", key, iv);
        String plain = GmCryptoTool.sm4DecryptCbcBase64(cipher, key, iv);
        assertEquals("hello", plain);
    }

    @Test
    void sm4GcmEncryptThenDecrypt_returnsOriginal() {
        String key = base64Of(sequentialBytes(16, 1));
        String iv = base64Of(sequentialBytes(12, 65));
        String aad = base64Of("aad".getBytes(StandardCharsets.UTF_8));
        String cipher = GmCryptoTool.sm4EncryptGcmBase64("hello", key, iv, aad);
        String plain = GmCryptoTool.sm4DecryptGcmBase64(cipher, key, iv, aad);
        assertEquals("hello", plain);
    }

    @Test
    void zuc128EncryptThenDecrypt_returnsOriginal() {
        String key = base64Of(sequentialBytes(16, 1));
        String iv = base64Of(sequentialBytes(16, 81));
        String cipher = GmCryptoTool.zucEncryptBase64("hello", key, iv);
        String plain = GmCryptoTool.zucDecryptBase64(cipher, key, iv);
        assertEquals("hello", plain);
    }

    @Test
    void zuc256EncryptThenDecrypt_returnsOriginal() {
        String key = base64Of(sequentialBytes(32, 1));
        String iv = base64Of(sequentialBytes(25, 101));
        String cipher = GmCryptoTool.zuc256EncryptBase64("hello", key, iv);
        String plain = GmCryptoTool.zuc256DecryptBase64(cipher, key, iv);
        assertEquals("hello", plain);
    }

    private static byte[] sequentialBytes(int length, int start) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (start + i);
        }
        return data;
    }

    private static String base64Of(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
}
