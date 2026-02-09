package com.example.demo.common.web.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解，用于标记需要权限的类或方法。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 权限码数组。
     *
     * @return 权限码列表
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    String[] value();

    /**
     * 多权限匹配逻辑。
     *
     * @return 逻辑关系
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    Logical logical() default Logical.AND;
}
