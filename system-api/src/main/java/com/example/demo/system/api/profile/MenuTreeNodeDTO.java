package com.example.demo.system.api.profile;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 登录用户菜单树节点。
 */
@Data
public class MenuTreeNodeDTO implements Serializable {

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
    private List<MenuTreeNodeDTO> children = new ArrayList<>();
}
