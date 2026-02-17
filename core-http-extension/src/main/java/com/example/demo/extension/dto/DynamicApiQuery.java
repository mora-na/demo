package com.example.demo.extension.dto;

import com.example.demo.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 动态接口查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DynamicApiQuery extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String path;

    private String method;

    private String status;

    private String type;

    private String authMode;
}
