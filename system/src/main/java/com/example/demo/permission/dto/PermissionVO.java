package com.example.demo.permission.dto;

import lombok.Data;

/**
 * 权限展示对象，用于接口返回。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class PermissionVO {

    private Long id;
    private String code;
    private String name;
    private Integer status;
}
