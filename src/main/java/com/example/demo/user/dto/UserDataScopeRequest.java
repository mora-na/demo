package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserDataScopeRequest {

    @NotBlank
    @Size(max = 32)
    private String dataScopeType;

    @Size(max = 512)
    private String dataScopeValue;
}
