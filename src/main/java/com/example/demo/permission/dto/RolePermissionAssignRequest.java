package com.example.demo.permission.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolePermissionAssignRequest {

    private List<Long> permissionIds;
}
