package com.example.demo.system.api.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户自助资料更新命令。
 */
@Data
public class UserProfileUpdateCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nickName;
    private String phone;
    private String email;
    private String sex;
    private String remark;
}
