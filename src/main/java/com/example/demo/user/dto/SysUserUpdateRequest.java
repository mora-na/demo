package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 用户更新请求参数，允许部分字段可选修改。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class SysUserUpdateRequest {

    @Size(max = 64)
    private String userName;

    @Size(max = 64)
    private String nickName;

    @Size(max = 32)
    private String phone;

    @Size(max = 128)
    private String email;

    @Size(max = 16)
    private String sex;

    private Integer status;

    private Long deptId;

    @Size(max = 255)
    private String remark;
}
