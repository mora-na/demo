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
public @interface ExcelColumn {

    /**
     * 表头名称，留空时默认使用字段名。
     *
     * @return 表头名称
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String headerName() default "";

    /**
     * 是否参与导入导出，exit=false 时忽略该字段。
     *
     * @return 是否导入导出
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    boolean exit() default true;

    /**
     * 自定义值映射，格式 "源值:目标值"，导出可替换，导入可反向映射。
     *
     * @return 映射规则数组
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String[] mapping() default {};
}
