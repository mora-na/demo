package com.example.demo.extension.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.extension.api.handler.DynamicApiHandler;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.dto.DynamicApiBeanMeta;
import com.example.demo.extension.dto.DynamicApiMetricsSnapshot;
import com.example.demo.extension.dto.DynamicApiRateLimitPolicyMeta;
import com.example.demo.extension.dto.DynamicApiTypeMeta;
import com.example.demo.extension.executor.ExecuteStrategyFactory;
import com.example.demo.extension.metrics.DynamicApiMetrics;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 动态接口元数据查询。
 */
@RestController
@RequestMapping("/dynamic-api/metadata")
@RequiredArgsConstructor
public class DynamicApiMetadataController extends BaseController {

    private static final String BASE_PACKAGE = "com.example.demo";

    private final ApplicationContext applicationContext;
    private final DynamicApiProperties properties;
    private final ExecuteStrategyFactory strategyFactory;
    private final DynamicApiMetrics metrics;

    @GetMapping("/beans")
    @RequirePermission("dynamic-api:query")
    public CommonResult<List<DynamicApiBeanMeta>> listBeans() {
        return success(loadBeanMetas());
    }

    @GetMapping("/rate-limit-policies")
    @RequirePermission("dynamic-api:query")
    public CommonResult<List<DynamicApiRateLimitPolicyMeta>> listRateLimitPolicies() {
        return success(loadRateLimitPolicies());
    }

    @GetMapping("/types")
    @RequirePermission("dynamic-api:query")
    public CommonResult<List<DynamicApiTypeMeta>> listTypes() {
        return success(strategyFactory.listTypes());
    }

    @GetMapping("/metrics")
    @RequirePermission("dynamic-api:query")
    public CommonResult<DynamicApiMetricsSnapshot> metrics() {
        return success(metrics.snapshot());
    }

    private List<DynamicApiBeanMeta> loadBeanMetas() {
        List<DynamicApiBeanMeta> result = new ArrayList<>();
        if (applicationContext == null) {
            return result;
        }
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            if (StringUtils.isBlank(beanName) || beanName.startsWith("scopedTarget.")) {
                continue;
            }
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception ex) {
                continue;
            }
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            if (targetClass == null) {
                continue;
            }
            if (!targetClass.getName().startsWith(BASE_PACKAGE)) {
                continue;
            }
            if (isExcludedComponent(targetClass)) {
                continue;
            }
            if (!DynamicApiHandler.class.isAssignableFrom(targetClass)) {
                continue;
            }
            DynamicApiBeanMeta meta = new DynamicApiBeanMeta();
            meta.setBeanName(beanName);
            meta.setClassName(targetClass.getName());
            result.add(meta);
        }
        result.sort(Comparator.comparing(DynamicApiBeanMeta::getBeanName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    private boolean isExcludedComponent(Class<?> targetClass) {
        return AnnotationUtils.findAnnotation(targetClass, Controller.class) != null
                || AnnotationUtils.findAnnotation(targetClass, RestController.class) != null
                || AnnotationUtils.findAnnotation(targetClass, Configuration.class) != null;
    }


    private List<DynamicApiRateLimitPolicyMeta> loadRateLimitPolicies() {
        List<DynamicApiRateLimitPolicyMeta> list = new ArrayList<>();
        if (properties == null || properties.getRateLimitPolicies() == null) {
            return list;
        }
        for (DynamicApiProperties.RateLimitPolicy policy : properties.getRateLimitPolicies()) {
            if (policy == null || StringUtils.isBlank(policy.getId())) {
                continue;
            }
            DynamicApiRateLimitPolicyMeta meta = new DynamicApiRateLimitPolicyMeta();
            meta.setId(policy.getId());
            meta.setName(StringUtils.trimToNull(policy.getName()));
            meta.setWindowSeconds(policy.getWindowSeconds());
            meta.setMaxRequests(policy.getMaxRequests());
            meta.setKeyMode(policy.getKeyMode());
            meta.setIncludePath(policy.isIncludePath());
            list.add(meta);
        }
        list.sort(Comparator.comparing(DynamicApiRateLimitPolicyMeta::getId, String.CASE_INSENSITIVE_ORDER));
        return list;
    }
}
