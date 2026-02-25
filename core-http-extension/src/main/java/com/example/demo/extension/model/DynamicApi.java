package com.example.demo.extension.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 动态接口实体，映射 demo_extension.dynamic_api 表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/17
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("demo_extension.dynamic_api")
public class DynamicApi extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口路径。
     */
    @TableField("path")
    private String path;

    /**
     * HTTP 方法。
     */
    @TableField("method")
    private String method;

    /**
     * 生命周期状态。
     */
    @TableField("status")
    private String status;

    /**
     * 执行类型。
     */
    @TableField("type")
    private String type;

    /**
     * 执行配置 JSON。
     */
    @TableField("config")
    private String config;

    /**
     * 认证模式。
     */
    @TableField("auth_mode")
    private String authMode;

    /**
     * 限流策略标识。
     */
    @TableField("rate_limit_policy")
    private String rateLimitPolicy;

    /**
     * 超时毫秒。
     */
    @TableField("timeout_ms")
    private Integer timeoutMs;
}
