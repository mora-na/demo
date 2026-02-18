package com.example.demo.extension.dto;

import lombok.Data;

import java.util.List;

/**
 * 动态接口 Bean 元数据。
 */
@Data
public class DynamicApiBeanMeta {

    private String beanName;

    private String className;

    private List<DynamicApiBeanMethod> methods;
}
