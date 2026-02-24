package com.example.demo.config.support;

import com.example.demo.config.entity.SysConfig;

/**
 * 配置操作结果。
 */
public class ConfigOperationResult {

    private final SysConfig config;
    private final String errorMessageKey;

    private ConfigOperationResult(SysConfig config, String errorMessageKey) {
        this.config = config;
        this.errorMessageKey = errorMessageKey;
    }

    public static ConfigOperationResult success(SysConfig config) {
        return new ConfigOperationResult(config, null);
    }

    public static ConfigOperationResult failed(String errorMessageKey) {
        return new ConfigOperationResult(null, errorMessageKey);
    }

    public boolean isSuccess() {
        return config != null;
    }

    public SysConfig getConfig() {
        return config;
    }

    public String getErrorMessageKey() {
        return errorMessageKey;
    }
}
