package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UserRoleAssignRequest {

    @NotNull
    private List<Long> roleIds;
}
