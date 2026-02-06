package com.example.demo.framework.tools;

import java.lang.annotation.*;

/**
 * 标记Excel导入导出的表头映射。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelColumn {

    /**
     * 表头名称，留空时默认使用字段名。
     */
    String headerName() default "";

    /**
     * 是否参与导入导出，exit=false 时忽略该字段。
     */
    boolean exit() default true;

    /**
     * 自定义值映射，格式 "源值:目标值"，如 {"0:女","1:男"}，匹配到则导出时替换为目标值，导入时可将目标值反向映射回源值。
     */
    String[] mapping() default {};
}
