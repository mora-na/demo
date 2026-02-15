package com.example.demo.post.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 岗位状态更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class PostStatusRequest {

    @NotNull
    private Integer status;
}
