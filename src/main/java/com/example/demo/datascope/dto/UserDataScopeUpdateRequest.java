package com.example.demo.datascope.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 用户数据范围覆盖更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class UserDataScopeUpdateRequest {

    @Size(max = 32)
    private String dataScopeType;

    @Size(max = 512)
    private String dataScopeValue;

    private Integer status;

    @Size(max = 500)
    private String remark;
}
