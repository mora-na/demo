package com.example.demo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应载体，返回验证码 ID、Base64 图像与有效期秒数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponse {
    private String captchaId;
    private String imageBase64;
    private int expireSeconds;
}
