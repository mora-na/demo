package com.example.demo.config.dto;

import com.example.demo.config.api.enums.ConfigValueType;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 配置更新请求。
 */
@Data
public class ConfigUpdateRequest {

    @NotBlank
    private String key;

    private String group;

    @NotBlank
    private String value;

    private ConfigValueType type;

    private String schema;

    private Integer status;

    private Integer hotUpdate;

    private Integer sensitive;

    private String remark;
}
