package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户数据范围更新请求体，指定范围类型与可选值。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class SysUserDataScopeRequest {

    @NotBlank
    @Size(max = 32)
    private String dataScopeType;

    @Size(max = 512)
    private String dataScopeValue;
}
