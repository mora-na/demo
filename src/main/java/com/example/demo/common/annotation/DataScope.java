package com.example.demo.common.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据范围注解，声明当前方法需要应用的数据权限范围。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    /**
     * 数据范围标识（通常为菜单权限标识，如 biz:order:list）。
     */
    @AliasFor("permission")
    String scopeKey() default "";

    /**
     * 权限标识别名，等同于 scopeKey。
     */
    @AliasFor("scopeKey")
    String permission() default "";

    /**
     * 部门字段所在表别名（可选）。
     */
    String deptAlias() default "";

    /**
     * 用户字段所在表别名（可选）。
     */
    String userAlias() default "";
}
