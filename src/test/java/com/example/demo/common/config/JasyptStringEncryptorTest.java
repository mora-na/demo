package com.example.demo.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JasyptStringEncryptorTest {

    @Test
    void encryptThenDecrypt_returnsOriginal() {
        JasyptStringEncryptor encryptor = new JasyptStringEncryptor("password");
        String cipher = encryptor.encrypt("secret");
        assertNotEquals("secret", cipher);
        assertEquals("secret", encryptor.decrypt(cipher));
    }
}
