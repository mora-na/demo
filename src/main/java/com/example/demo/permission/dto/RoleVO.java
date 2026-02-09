package com.example.demo.permission.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoleVO {

    private Long id;
    private String code;
    private String name;
    private Integer status;
    private List<Long> permissionIds;
}
