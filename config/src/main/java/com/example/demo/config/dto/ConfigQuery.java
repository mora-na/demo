package com.example.demo.config.dto;

import com.example.demo.common.model.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 配置查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigQuery extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String group;

    private String key;

    private String type;

    private Integer status;

    private Integer hotUpdate;

    private Integer sensitive;
}
