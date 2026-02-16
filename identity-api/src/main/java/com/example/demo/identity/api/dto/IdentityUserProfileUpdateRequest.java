package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 身份域对外暴露的“当前用户资料更新”命令。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class IdentityUserProfileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nickName;
    private String phone;
    private String email;
    private String sex;
    private String remark;
}
