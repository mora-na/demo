package com.example.demo.datascope.dto;

import lombok.Data;

import java.util.List;

/**
 * 用户数据范围覆盖详情响应。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class UserDataScopeDetailResponse {

    private Long userId;
    private String userName;
    private String nickName;
    private Long deptId;
    private String deptName;
    private List<UserDataScopeVO> overrides;
}
