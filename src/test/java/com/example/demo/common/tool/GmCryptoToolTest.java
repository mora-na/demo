package com.example.demo.common.tool;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

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
}
