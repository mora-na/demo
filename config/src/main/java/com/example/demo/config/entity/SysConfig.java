package com.example.demo.config.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 系统配置实体。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("demo_config.sys_config")
public class SysConfig extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("config_key")
    private String configKey;

    @TableField("config_group")
    private String configGroup;

    @TableField("config_value")
    private String configValue;

    @TableField("config_type")
    private String configType;

    @TableField("config_schema")
    private String configSchema;

    @TableField("config_version")
    private Integer configVersion;

    @TableField("status")
    private Integer status;

    @TableField("hot_update")
    private Integer hotUpdate;

    @TableField("config_sensitive")
    private Integer configSensitive;
}
