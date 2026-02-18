package com.example.demo.extension.model;

import lombok.Data;

/**
 * Bean 执行配置。
 */
@Data
public class BeanExecuteConfig {

    /**
     * Spring Bean 名称。
     */
    private String beanName;

    /**
     * 参数模式。
     */
    private String paramMode;

    /**
     * 参数结构描述（JSON 文本）。
     */
    private String paramSchema;
}
