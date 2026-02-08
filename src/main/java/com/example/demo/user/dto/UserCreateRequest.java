package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String userName;

    @Size(max = 64)
    private String nickName;

    @NotBlank
    @Size(min = 6, max = 128)
    private String password;

    @Size(max = 16)
    private String sex;

    private Integer status;

    private String dataScopeType;

    private String dataScopeValue;

    @Size(max = 255)
    private String tst;
}
