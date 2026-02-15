package com.example.demo.datascope.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户数据范围覆盖创建请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class UserDataScopeCreateRequest {

    /**
     * scope_key，菜单权限标识或 * 表示全局覆盖。
     */
    @NotBlank
    @Size(max = 200)
    private String scopeKey;

    @NotBlank
    @Size(max = 32)
    private String dataScopeType;

    @Size(max = 512)
    private String dataScopeValue;

    private Integer status;

    @Size(max = 500)
    private String remark;
}
