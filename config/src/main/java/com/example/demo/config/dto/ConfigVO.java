package com.example.demo.config.dto;

import com.example.demo.config.api.enums.ConfigValueType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 配置视图对象。
 */
@Data
public class ConfigVO {

    private Long id;

    private String group;

    private String key;

    private String value;

    private ConfigValueType type;

    private String schema;

    private Integer status;

    private Integer hotUpdate;

    private Integer sensitive;

    private Integer configVersion;

    private String remark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
