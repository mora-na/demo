package com.example.demo.framework.annotation;

import java.lang.annotation.*;

/**
 * 标记一组用于联合匹配的普通字段（非联合主键）。
 * 由 {@link com.example.demo.framework.service.impl.MppServiceImpl} 在查询/保存/更新时读取。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MppMultiField {

    /**
     * 数据库列名，留空则自动使用实体字段名对应的列。
     */
    String value() default "";
}
