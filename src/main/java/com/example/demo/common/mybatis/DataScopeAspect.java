package com.example.demo.common.mybatis;

import com.example.demo.common.annotation.DataScope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 数据范围注解切面，将 scopeKey 等信息写入上下文。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Aspect
@Component
public class DataScopeAspect {

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        DataScopeContextHolder.DataScopeRequest previous = DataScopeContextHolder.get();
        String scopeKey = resolveScopeKey(dataScope);
        DataScopeContextHolder.set(new DataScopeContextHolder.DataScopeRequest(
                scopeKey,
                emptyToNull(dataScope.deptAlias()),
                emptyToNull(dataScope.userAlias())
        ));
        try {
            return joinPoint.proceed();
        } finally {
            if (previous != null) {
                DataScopeContextHolder.set(previous);
            } else {
                DataScopeContextHolder.clear();
            }
        }
    }

    private String resolveScopeKey(DataScope dataScope) {
        if (dataScope == null) {
            return null;
        }
        if (StringUtils.hasText(dataScope.scopeKey())) {
            return dataScope.scopeKey().trim();
        }
        if (StringUtils.hasText(dataScope.permission())) {
            return dataScope.permission().trim();
        }
        return null;
    }

    private String emptyToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
