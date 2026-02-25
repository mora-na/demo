package com.example.demo.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记单个配置字段的特性（例如是否允许热更新）。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigField {

    /**
     * 是否允许热更新。默认关闭，只有显式设置为 true 才允许热更新。
     */
    boolean hotUpdate() default false;

    /**
     * 是否参与配置种子。默认关闭，只有显式设置为 true 才会参与种子。
     */
    boolean seed() default false;
}
