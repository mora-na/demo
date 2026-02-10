package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * Excel 导入导出字段注解，声明列标题与值映射规则。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Excel {

    /**
     * 表头名称，留空时默认使用字段名。
     *
     * @return 表头名称
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String value() default "";

    /**
     * 表头名称（推荐使用），留空时默认使用字段名。
     *
     * @return 表头名称
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String header() default "";

    /**
     * 自定义值映射，格式 "源值:目标值"，导出可替换，导入可反向映射。
     *
     * @return 映射规则数组
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String[] mapping() default {};

    /**
     * 排序值，值越小越靠前；默认不参与排序。
     *
     * @return 排序值
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    int sort() default Integer.MIN_VALUE;
}
