package com.example.demo.menu.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 菜单创建请求参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class MenuCreateRequest {

    @NotBlank
    @Size(max = 128)
    private String name;

    @Size(max = 64)
    private String code;

    private Long parentId;

    @Size(max = 255)
    private String path;

    @Size(max = 255)
    private String component;

    @Size(max = 64)
    private String permission;

    private Integer status;

    private Integer sort;

    @Size(max = 255)
    private String remark;
}
