package com.example.demo.log.annotation;

import com.example.demo.log.enums.BusinessType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    String title() default "";

    String operation() default "";

    BusinessType businessType() default BusinessType.OTHER;

    boolean saveParam() default true;

    boolean saveResult() default false;

    boolean saveDiff() default false;

    String[] excludeParams() default {"password", "oldPassword", "newPassword", "token"};
}
