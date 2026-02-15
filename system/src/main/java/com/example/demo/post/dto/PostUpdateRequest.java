package com.example.demo.post.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 岗位更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class PostUpdateRequest {

    @Size(max = 128)
    private String name;

    @Size(max = 64)
    private String code;

    private Long deptId;

    private Integer status;

    private Integer sort;

    @Size(max = 255)
    private String remark;
}
