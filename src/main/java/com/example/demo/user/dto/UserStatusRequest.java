package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserStatusRequest {

    @NotNull
    private Integer status;
}
