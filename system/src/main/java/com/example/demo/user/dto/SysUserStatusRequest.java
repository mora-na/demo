package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 用户状态更新请求体，限定状态为必填。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class SysUserStatusRequest {

    @NotNull
    private Integer status;
}
