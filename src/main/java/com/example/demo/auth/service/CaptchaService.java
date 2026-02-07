package com.example.demo.auth.service;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.CaptchaResponse;
import com.example.demo.auth.store.CaptchaStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String IMAGE_PREFIX = "data:image/png;base64,";

    private final AuthProperties authProperties;
    private final CaptchaStore captchaStore;

    public CaptchaResponse createCaptcha() {
        AuthProperties.Captcha config = authProperties.getCaptcha();
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(
                config.getWidth(),
                config.getHeight(),
                config.getCodeLength(),
                config.getThickness()
        );
        String captchaId = UUID.randomUUID().toString();
        long expireAt = Instant.now().getEpochSecond() + config.getExpireSeconds();
        captchaStore.save(captchaId, captcha.getCode(), expireAt);
        String imageBase64 = IMAGE_PREFIX + captcha.getImageBase64();
        return new CaptchaResponse(captchaId, imageBase64, config.getExpireSeconds());
    }

    public boolean verify(String captchaId, String captchaCode) {
        return captchaStore.verifyAndRemove(captchaId, captchaCode);
    }
}
