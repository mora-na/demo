package com.example.demo.identity.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 身份域对外暴露的菜单树节点。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/16
 */
@Data
public class IdentityMenuTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    private Long parentId;
    private String path;
    private String component;
    private String permission;
    private Integer status;
    private Integer sort;
    private String remark;
    private List<IdentityMenuTreeDTO> children = new ArrayList<>();
}
