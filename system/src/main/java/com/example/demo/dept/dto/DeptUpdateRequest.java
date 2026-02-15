package com.example.demo.dept.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 部门更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class DeptUpdateRequest {

    @Size(max = 128)
    private String name;

    @Size(max = 64)
    private String code;

    private Long parentId;

    private Integer status;

    private Integer sort;

    @Size(max = 255)
    private String remark;
}
