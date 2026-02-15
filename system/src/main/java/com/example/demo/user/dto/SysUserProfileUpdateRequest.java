package com.example.demo.user.dto;

import lombok.Data;

/**
 * 当前登录用户资料更新请求（仅包含用户资料字段，不含密码字段）。
 */
@Data
public class SysUserProfileUpdateRequest {

    private String nickName;

    private String phone;

    private String email;

    private String sex;

    private String remark;
}
