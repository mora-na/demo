package com.example.demo.common.config;

import com.example.demo.common.tool.GmCryptoTool;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class jasyptStringEncryptorTest {

    @Test
    void encryptThenDecrypt_returnsOriginal() {
        JasyptStringEncryptor encryptor = new JasyptStringEncryptor("password");
        String cipher = encryptor.encrypt("secret");
        assertNotEquals("secret", cipher);
        assertTrue(cipher.startsWith("SM4GCM:"));
        assertEquals("secret", encryptor.decrypt(cipher));
    }

    @Test
    void decrypt_legacyEcbCipher_returnsOriginal() {
        JasyptStringEncryptor encryptor = new JasyptStringEncryptor("password");
        byte[] digest = GmCryptoTool.sm3Digest("password");
        String base64Key = Base64.getEncoder().encodeToString(Arrays.copyOf(digest, 16));
        String legacyCipher = GmCryptoTool.sm4EncryptEcbBase64("secret", base64Key);
        assertEquals("secret", encryptor.decrypt(legacyCipher));
    }
}
