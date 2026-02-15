package com.example.demo.system.api.dept;

import lombok.Data;

import java.io.Serializable;

/**
 * 部门简要信息。
 */
@Data
public class DeptDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
}
