package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class UserUpdateRequest {

    @Size(max = 64)
    private String userName;

    @Size(max = 64)
    private String nickName;

    @Size(max = 16)
    private String sex;

    private Integer status;

    @Size(max = 255)
    private String tst;
}
