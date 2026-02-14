package com.example.demo.post.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 岗位视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String code;

    private Long deptId;

    private Integer status;

    private Integer sort;

    private String remark;
}
