package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class UserResetPasswordRequest {

    @NotBlank
    @Size(min = 6, max = 128)
    private String newPassword;
}
