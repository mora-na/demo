package com.example.demo.menu.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
public class MenuTreeVO implements Serializable {

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

    private List<MenuTreeVO> children = new ArrayList<>();
}
