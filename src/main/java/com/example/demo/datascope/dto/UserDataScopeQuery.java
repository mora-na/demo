package com.example.demo.datascope.dto;

import lombok.Data;

/**
 * 用户数据范围覆盖查询条件。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class UserDataScopeQuery {

    private String userName;
    private String menuKeyword;
    private Integer status;
}
