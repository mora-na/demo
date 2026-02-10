package com.example.demo.dept.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 部门状态更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class DeptStatusRequest {

    @NotNull
    private Integer status;
}
