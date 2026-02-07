package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {

    @Test
    void matches_plainMode() {
        AuthProperties properties = new AuthProperties();
        properties.getPassword().setMode("plain");
        PasswordService service = new PasswordService(properties);

        assertTrue(service.matches("secret", "secret"));
        assertFalse(service.matches("secret", "other"));
    }

    @Test
    void matches_md5Mode() {
        AuthProperties properties = new AuthProperties();
        properties.getPassword().setMode("md5");
        properties.getPassword().setSalt("salt-");
        PasswordService service = new PasswordService(properties);

        String encoded = service.encode("pw");
        assertTrue(service.matches("pw", encoded));
        assertFalse(service.matches("pw", "bad"));
    }

    @Test
    void matches_bcryptMode() {
        AuthProperties properties = new AuthProperties();
        properties.getPassword().setMode("bcrypt");
        PasswordService service = new PasswordService(properties);

        String encoded = service.encode("pw");
        assertNotEquals("pw", encoded);
        assertTrue(service.matches("pw", encoded));
    }
}
