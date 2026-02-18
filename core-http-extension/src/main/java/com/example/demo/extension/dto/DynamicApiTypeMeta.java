package com.example.demo.extension.dto;

import lombok.Data;

/**
 * 动态接口类型元数据。
 */
@Data
public class DynamicApiTypeMeta {

    /**
     * 类型编码。
     */
    private String code;

    /**
     * 展示名称。
     */
    private String name;
}
