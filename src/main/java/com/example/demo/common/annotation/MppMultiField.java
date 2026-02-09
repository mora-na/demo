package com.example.demo.common.annotation;

import java.lang.annotation.*;

/**
 * 多字段联合匹配注解，用于声明非联合主键的匹配字段集合。
 * 由 {@link com.example.demo.common.mybatis.MppServiceImpl} 在查询/保存/更新时读取。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MppMultiField {

    /**
     * 数据库列名，留空则自动使用实体字段名对应的列。
     *
     * @return 数据库列名
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String value() default "";
}
