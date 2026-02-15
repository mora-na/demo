package com.example.demo.datascope.dto;

import lombok.Data;

/**
 * 用户全菜单数据范围解析摘要。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
public class DataScopeResolveMenuVO {

    private Long menuId;
    private String menuName;
    private String permission;
    private String finalScopeLabel;
    private String sourceLayer;
}
