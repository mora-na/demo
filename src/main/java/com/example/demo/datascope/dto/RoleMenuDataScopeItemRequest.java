package com.example.demo.datascope.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 角色菜单级数据范围单项请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class RoleMenuDataScopeItemRequest {

    @NotNull
    private Long menuId;

    /**
     * 数据范围类型（ALL/DEPT/DEPT_AND_CHILD/CUSTOM_DEPT/SELF/NONE/INHERIT）。
     */
    @Size(max = 32)
    private String dataScopeType;

    /**
     * 自定义部门 ID 列表（仅在 CUSTOM/CUSTOM_DEPT 时生效）。
     */
    private List<Long> customDeptIds;
}
