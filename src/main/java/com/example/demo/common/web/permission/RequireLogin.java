package com.example.demo.common.web.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 登录校验注解，用于标记必须登录访问的类或方法。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireLogin {
}
