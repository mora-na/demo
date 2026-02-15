package com.example.demo.system.api.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 跨域用户简要信息。
 */
@Data
public class UserSimpleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String nickName;
}
