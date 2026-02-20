package com.example.demo.common.datasource;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

/**
 * 按模块边界自动切换数据源，结合模块账号权限实现数据库层隔离。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ModuleDataSourceRoutingAspect {

    private final ModuleDataSourceRoutingProperties properties;

    public ModuleDataSourceRoutingAspect(ModuleDataSourceRoutingProperties properties) {
        this.properties = properties;
    }

    @Around(
            "execution(* com.example.demo..controller..*(..))"
                    + " || execution(* com.example.demo..service..*(..))"
                    + " || execution(* com.example.demo..mapper..*(..))"
                    + " || execution(* com.example.demo..facade..*(..))"
                    + " || execution(* com.example.demo.common.mybatis.DataScopeRuleProvider.*(..))"
                    + " || execution(* com.example.demo.common.web.permission.PermissionService.*(..))"
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

        ModuleDataSourceRoutingProperties.ModuleConfig module = resolveModule(targetClass.getName());
        if (module == null || !StringUtils.hasText(module.getRwDataSource())) {
            return joinPoint.proceed();
        }

        boolean readOnly = isReadOnlyOperation(specificMethod, targetClass);
        String dataSource = resolveDataSource(module, readOnly);
        if (!StringUtils.hasText(dataSource)) {
            return joinPoint.proceed();
        }

        DynamicDataSourceContextHolder.push(dataSource);
        try {
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    private String resolveDataSource(ModuleDataSourceRoutingProperties.ModuleConfig module, boolean readOnly) {
        String rw = module.getRwDataSource();
        String ro = module.getRoDataSource();
        if (module.isForceReadWrite() || !readOnly) {
            return rw;
        }
        return StringUtils.hasText(ro) ? ro : rw;
    }

    private boolean isReadOnlyOperation(Method method, Class<?> targetClass) {
        if (AnnotatedElementUtils.hasAnnotation(method, ReadOnlyRoute.class)
                || AnnotatedElementUtils.hasAnnotation(targetClass, ReadOnlyRoute.class)) {
            return true;
        }
        Transactional tx = AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        if (tx == null) {
            tx = AnnotatedElementUtils.findMergedAnnotation(targetClass, Transactional.class);
        }
        if (tx != null) {
            return tx.readOnly();
        }
        ModuleDataSourceRoutingProperties.ReadOnlyDetection detection = properties.getReadOnlyDetection();
        if (detection != ModuleDataSourceRoutingProperties.ReadOnlyDetection.EXPLICIT_OR_METHOD_NAME) {
            return false;
        }
        List<String> prefixes = properties.getReadOnlyMethodPrefixes();
        if (prefixes == null || prefixes.isEmpty()) {
            return false;
        }
        String methodName = method.getName().toLowerCase(Locale.ROOT);
        for (String prefix : prefixes) {
            if (prefix == null || prefix.trim().isEmpty()) {
                continue;
            }
            if (methodName.startsWith(prefix.trim().toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private ModuleDataSourceRoutingProperties.ModuleConfig resolveModule(String className) {
        List<ModuleDataSourceRoutingProperties.ModuleConfig> modules = properties.getModules();
        if (modules == null || modules.isEmpty()) {
            return null;
        }
        for (ModuleDataSourceRoutingProperties.ModuleConfig module : modules) {
            if (matchesModule(module, className)) {
                return module;
            }
        }
        return null;
    }

    private boolean matchesModule(ModuleDataSourceRoutingProperties.ModuleConfig module, String className) {
        List<String> classes = module.getClasses();
        if (classes != null) {
            for (String candidate : classes) {
                if (className.equals(candidate)) {
                    return true;
                }
            }
        }
        List<String> packages = module.getPackages();
        if (packages != null) {
            for (String pkg : packages) {
                String normalized = normalizePackage(pkg);
                if (normalized != null && className.startsWith(normalized)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizePackage(String pkg) {
        if (pkg == null) {
            return null;
        }
        String trimmed = pkg.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.endsWith(".") ? trimmed : trimmed + ".";
    }
}
