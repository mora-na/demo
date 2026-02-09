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

/**
 * 验证码服务，生成图片验证码并进行验证。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final String IMAGE_PREFIX = "data:image/png;base64,";

    private final AuthProperties authProperties;
    private final CaptchaStore captchaStore;

    /**
     * 生成验证码并缓存验证码值。
     *
     * @return 验证码响应信息
     */
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

    /**
     * 校验验证码并在校验后删除缓存。
     *
     * @param captchaId   验证码 ID
     * @param captchaCode 用户输入的验证码
     * @return 校验通过返回 true
     */
    public boolean verify(String captchaId, String captchaCode) {
        return captchaStore.verifyAndRemove(captchaId, captchaCode);
    }
}
