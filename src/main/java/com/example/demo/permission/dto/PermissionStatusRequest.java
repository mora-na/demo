package com.example.demo.permission.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PermissionStatusRequest {

    @NotNull
    private Integer status;
}
