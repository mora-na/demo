package com.example.demo.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户岗位分配请求体，携带岗位 ID 列表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class SysUserPostAssignRequest {

    @NotNull
    private List<Long> postIds;
}
