package com.example.demo.datascope.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户数据范围覆盖视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class UserDataScopeVO {

    private Long id;
    private Long userId;
    private String userName;
    private String nickName;
    private Long deptId;
    private String deptName;
    private String scopeKey;
    private String menuName;
    private String permission;
    private String dataScopeType;
    private String dataScopeValue;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
