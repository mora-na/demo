package com.example.demo.system.api.user;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 跨域用户账户信息。
 */
@Data
public class UserAccountDTO implements Serializable {

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    public static final int FORCE_PASSWORD_CHANGE_YES = 1;
    public static final int FORCE_PASSWORD_CHANGE_NO = 0;

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
    private String password;
    private LocalDateTime passwordUpdatedAt;
    private Integer forcePasswordChange;
    private Integer status;
    private Long deptId;
    private String dataScopeType;
    private String dataScopeValue;
    private String sex;
    private String remark;
}
