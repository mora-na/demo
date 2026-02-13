package com.example.demo.auth.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 当前登录用户资料更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class UserProfileUpdateRequest {

    @Size(max = 64)
    private String nickName;

    @Size(max = 16)
    private String sex;

    @Size(max = 255)
    private String remark;

    private String oldPassword;

    private String newPassword;
}
