package com.example.demo.permission.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色展示对象，用于接口返回。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class RoleVO {

    private Long id;
    private String code;
    private String name;
    private Integer status;
    private List<Long> permissionIds;
}
