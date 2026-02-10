package com.example.demo.menu.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 菜单状态更新请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class MenuStatusRequest {

    @NotNull
    private Integer status;
}
