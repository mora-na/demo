package com.example.demo.config;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 按模块边界自动切换数据源，结合模块账号权限实现数据库层隔离。
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ModuleDataSourceRoutingAspect {

    private static final Set<String> READ_PREFIXES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "get", "find", "list", "page", "query", "select", "count",
            "search", "load", "read", "fetch", "resolve", "exists"
    )));

    @Around(
            "execution(* com.example.demo..controller..*(..))"
                    + " || execution(* com.example.demo..service..*(..))"
                    + " || execution(* com.example.demo..mapper..*(..))"
                    + " || execution(* com.example.demo..facade..*(..))"
                    + " || execution(* com.example.demo.common.mybatis.DbDataScopeRuleProvider.*(..))"
                    + " || execution(* com.example.demo.common.web.permission.DbPermissionService.*(..))"
    )
    public Object route(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        if (target == null) {
            return joinPoint.proceed();
        }
        Class<?> targetClass = AopUtils.getTargetClass(target);
        if (targetClass == null) {
            return joinPoint.proceed();
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);
        if (AnnotatedElementUtils.hasAnnotation(specificMethod, DS.class)
                || AnnotatedElementUtils.hasAnnotation(targetClass, DS.class)) {
            return joinPoint.proceed();
        }

        ModuleDataSource module = resolveModule(targetClass.getName());
        if (module == null) {
            return joinPoint.proceed();
        }

        boolean readOnly = isReadOnlyOperation(specificMethod, targetClass);
        String dataSource = module.resolveDataSource(readOnly);
        DynamicDataSourceContextHolder.push(dataSource);
        try {
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private boolean isReadOnlyOperation(Method method, Class<?> targetClass) {
        Transactional tx = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        if (tx == null) {
            tx = AnnotatedElementUtils.findMergedAnnotation(targetClass, Transactional.class);
        }
        if (tx != null) {
            return tx.readOnly();
        }
        String methodName = method.getName().toLowerCase(Locale.ROOT);
        for (String prefix : READ_PREFIXES) {
            if (methodName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private ModuleDataSource resolveModule(String className) {
        if (className.startsWith("com.example.demo.order.")) {
            return ModuleDataSource.ORDER;
        }
        if (className.startsWith("com.example.demo.notice.")) {
            return ModuleDataSource.NOTICE;
        }
        if (className.startsWith("com.example.demo.job.")) {
            return ModuleDataSource.JOB;
        }
        if (className.startsWith("com.example.demo.log.")) {
            return ModuleDataSource.LOG;
        }
        if (className.startsWith("com.example.demo.dict.")) {
            return ModuleDataSource.DICT;
        }
        if (className.startsWith("com.example.demo.common.cache.")) {
            return ModuleDataSource.CACHE;
        }
        if (className.startsWith("com.example.demo.user.")
                || className.startsWith("com.example.demo.dept.")
                || className.startsWith("com.example.demo.menu.")
                || className.startsWith("com.example.demo.permission.")
                || className.startsWith("com.example.demo.post.")
                || className.startsWith("com.example.demo.datascope.")
                || className.startsWith("com.example.demo.identity.facade.")
                || "com.example.demo.common.mybatis.DbDataScopeRuleProvider".equals(className)
                || "com.example.demo.common.web.permission.DbPermissionService".equals(className)) {
            return ModuleDataSource.SYSTEM;
        }
        return null;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ModuleDataSource {
        SYSTEM("system_rw", "system_ro", false),
        ORDER("order_rw", "order_ro", false),
        NOTICE("notice_rw", "notice_ro", false),
        JOB("job_rw", "job_ro", false),
        LOG("log_rw", "log_ro", false),
        DICT("dict_rw", "dict_ro", false),
        CACHE("cache_rw", "cache_ro", true);

        private final String rwDataSource;
        private final String roDataSource;
        private final boolean forceReadWrite;

        public String resolveDataSource(boolean readOnly) {
            if (forceReadWrite) {
                return rwDataSource;
            }
            return readOnly ? roDataSource : rwDataSource;
        }
    }
}
