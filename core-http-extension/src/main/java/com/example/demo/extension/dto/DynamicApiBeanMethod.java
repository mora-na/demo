package com.example.demo.extension.dto;

import lombok.Data;

/**
 * 动态接口 Bean 方法描述。
 */
@Data
public class DynamicApiBeanMethod {

    private String name;

    private String signature;

    private String parameterType;
}
