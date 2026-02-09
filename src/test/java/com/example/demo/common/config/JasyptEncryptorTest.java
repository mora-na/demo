package com.example.demo.common.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JasyptEncryptorTest {

    @Test
    void encryptThenDecrypt_returnsOriginal() {
        JasyptEncryptor encryptor = new JasyptEncryptor("password");
        String cipher = encryptor.encrypt("secret");
        assertNotEquals("secret", cipher);
        assertEquals("secret", encryptor.decrypt(cipher));
    }
}
