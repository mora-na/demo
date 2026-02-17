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
     * Bean 方法名。
     */
    private String method;
}
