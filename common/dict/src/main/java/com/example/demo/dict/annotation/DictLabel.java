package com.example.demo.dict.annotation;

import com.example.demo.dict.serializer.DictLabelSerializer;
import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/**
 * 字典翻译注解。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = DictLabelSerializer.class)
public @interface DictLabel {

    /**
     * 字典类型。
     */
    String value();

    /**
     * 翻译结果写入字段名，默认=当前字段名+Label。
     */
    String target() default "";
}
