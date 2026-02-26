package com.example.demo.config.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配置变更流水。
 */
@Data
@TableName("demo_config.sys_config_change_log")
public class SysConfigChangeLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long id;

    @TableField("config_id")
    private Long configId;

    @TableField("config_key")
    private String configKey;

    @TableField("config_group")
    private String configGroup;

    @TableField("config_value_old")
    private String configValueOld;

    @TableField("config_value_new")
    private String configValueNew;

    @TableField("config_type")
    private String configType;

    @TableField("config_version")
    private Integer configVersion;

    @TableField("hot_update")
    private Integer hotUpdate;

    @TableField("config_sensitive")
    private Integer configSensitive;

    @TableField("change_type")
    private String changeType;

    @TableField("change_time")
    private LocalDateTime changeTime;

    @TableField("node_id")
    private String nodeId;

    @TableField("operator_id")
    private Long operatorId;
}
