package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户创建请求参数，包含基础信息与数据范围设置。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class SysUserCreateRequest {

    @NotBlank
    @Size(max = 64)
    private String userName;

    @Size(max = 64)
    private String nickName;

    @Size(max = 256)
    private String password;

    @Size(max = 16)
    private String sex;

    private Integer status;

    private Long deptId;

    private String dataScopeType;

    private String dataScopeValue;

    @Size(max = 255)
    private String remark;
}
