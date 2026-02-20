package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 身份域对外暴露的用户只读数据传输对象（不含密码凭据）。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class IdentityUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private String sex;
    private Integer status;
    private Long deptId;
    private String dataScopeType;
    private String dataScopeValue;
    private LocalDateTime passwordUpdatedAt;
    private Integer forcePasswordChange;
}
