package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户重置密码请求体，校验新密码的必填与长度约束。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class SysUserResetPasswordRequest {

    @NotBlank
    @Size(min = 6, max = 256)
    private String newPassword;
}
