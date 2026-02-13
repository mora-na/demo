package com.example.demo.auth.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 当前登录用户基础信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class UserProfileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String userName;

    private String nickName;

    private String phone;

    private String email;

    private String sex;

    private Long deptId;

    private String dataScopeType;

    private String dataScopeValue;
}
