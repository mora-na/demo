package com.example.demo.post.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 岗位创建请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class PostCreateRequest {

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 64)
    private String code;

    @NotNull
    private Long deptId;

    private Integer status;

    private Integer sort;

    @Size(max = 255)
    private String remark;
}
