package com.example.demo.auth.service;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.CaptchaResponse;
import com.example.demo.auth.store.CaptchaStore;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class CaptchaServiceTest {

    @Test
    void createCaptcha_returnsPayloadAndStoresCode() {
        AuthProperties properties = new AuthProperties();
        properties.getCaptcha().setExpireSeconds(90);
        CaptchaStore store = mock(CaptchaStore.class);
        CaptchaService service = new CaptchaService(properties, store);

        CaptchaResponse response = service.createCaptcha();
        assertNotNull(response.getCaptchaId());
        assertTrue(response.getImageBase64().startsWith("data:image/png;base64,"));

        ArgumentCaptor<String> idCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> expireCaptor = ArgumentCaptor.forClass(Long.class);
        verify(store).save(idCaptor.capture(), codeCaptor.capture(), expireCaptor.capture());
        assertNotNull(idCaptor.getValue());
        assertNotNull(codeCaptor.getValue());
        assertNotNull(expireCaptor.getValue());
    }
}
