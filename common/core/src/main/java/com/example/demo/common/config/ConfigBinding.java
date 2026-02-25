package com.example.demo.common.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记常量配置可由配置中心覆盖，并可选择是否支持热更新。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigBinding {

    /**
     * 配置分组（对应 config_group）。
     */
    String group();

    /**
     * 配置键前缀。
     */
    String prefix() default "constants.";

    /**
     * 是否允许热更新（仅影响通过 ConfigBinding 绑定的属性）。
     * 默认关闭，只有显式设置为 true 才允许热更新。
     */
    boolean hotUpdate() default false;

    /**
     * 是否启用默认种子（仅影响通过 ConfigBinding 绑定的属性）。
     * 默认关闭，只有显式设置为 true 才会参与种子。
     */
    boolean seed() default false;
}
