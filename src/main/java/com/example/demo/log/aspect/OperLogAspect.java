package com.example.demo.log.aspect;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.web.permission.RequireLogin;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dept.entity.Dept;
import com.example.demo.dept.service.DeptService;
import com.example.demo.log.annotation.OperLog;
import com.example.demo.log.entity.SysOperLog;
import com.example.demo.log.enums.BusinessType;
import com.example.demo.log.event.OperLogEvent;
import com.example.demo.log.support.IpUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 操作日志切面，记录写操作日志。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Slf4j
@Aspect
@Component
public class OperLogAspect {

    private static final int MAX_TEXT_LENGTH = 2000;
    private static final String[] DEFAULT_EXCLUDE_PARAMS = {"password", "oldPassword", "newPassword", "token"};
    private static final Map<String, String> TITLE_MAPPINGS;
    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{(.+?)}");

    static {
        Map<String, String> mappings = new HashMap<>();
        mappings.put("user", "用户管理");
        mappings.put("role", "角色管理");
        mappings.put("menu", "菜单管理");
        mappings.put("dept", "部门管理");
        mappings.put("post", "岗位管理");
        mappings.put("permission", "权限管理");
        mappings.put("notice", "系统通知");
        mappings.put("job", "定时任务");
        mappings.put("order", "订单管理");
        mappings.put("data-scope", "数据权限");
        mappings.put("log", "操作日志");
        mappings.put("login-log", "登录日志");
        TITLE_MAPPINGS = Collections.unmodifiableMap(mappings);
    }

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final DeptService deptService;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public OperLogAspect(ApplicationEventPublisher eventPublisher,
                         ObjectMapper objectMapper,
                         DeptService deptService) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.deptService = deptService;
    }

    @Pointcut("@annotation(com.example.demo.log.annotation.OperLog) || " +
            "@annotation(com.example.demo.common.web.permission.RequirePermission) || " +
            "@annotation(com.example.demo.common.web.permission.RequireLogin) || " +
            "@within(com.example.demo.common.web.permission.RequirePermission) || " +
            "@within(com.example.demo.common.web.permission.RequireLogin)")
    public void operLogPointcut() {
    }

    @Around("operLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget() == null ? signature.getDeclaringType() : joinPoint.getTarget().getClass();
        OperLog operLog = AnnotatedElementUtils.findMergedAnnotation(method, OperLog.class);
        RequirePermission permission = resolvePermission(method, targetClass);
        RequireLogin login = resolveLogin(method, targetClass);
        HttpServletRequest request = currentRequest();
        String httpMethod = request == null ? null : request.getMethod();

        boolean shouldRecord = shouldRecord(operLog, permission, login, httpMethod);
        if (!shouldRecord) {
            return joinPoint.proceed();
        }

        SysOperLog logEntity = new SysOperLog();
        logEntity.setMethod(signature.getDeclaringTypeName() + "." + signature.getName() + "()");
        fillBaseInfo(logEntity, request);
        fillOperatorInfo(logEntity);
        fillRequestInfo(logEntity, joinPoint, request, operLog);
        fillBizInfo(logEntity, operLog, permission, httpMethod);

        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            logEntity.setStatus(1);
            if (operLog != null && operLog.saveResult() && result != null) {
                logEntity.setOperResult(truncate(maskSensitive(toJson(result), operLog.excludeParams())));
            }
            return result;
        } catch (Throwable ex) {
            error = ex;
            logEntity.setStatus(0);
            logEntity.setErrorMsg(truncate(ex.getMessage()));
            throw ex;
        } finally {
            logEntity.setCostTime(System.currentTimeMillis() - startTime);
            logEntity.setOperTime(LocalDateTime.now());
            if (operLog != null && StringUtils.isNotBlank(operLog.operation())) {
                logEntity.setOperation(resolveSpel(operLog.operation(), signature, joinPoint.getArgs(), result));
            }
            if (error != null && StringUtils.isBlank(logEntity.getErrorMsg())) {
                logEntity.setErrorMsg(truncate(error.toString()));
            }
            eventPublisher.publishEvent(new OperLogEvent(logEntity));
        }
    }

    private boolean shouldRecord(OperLog operLog,
                                 RequirePermission permission,
                                 RequireLogin login,
                                 String httpMethod) {
        if (operLog != null) {
            return true;
        }
        if (httpMethod == null) {
            return false;
        }
        if ("GET".equalsIgnoreCase(httpMethod) || "OPTIONS".equalsIgnoreCase(httpMethod)) {
            return false;
        }
        return permission != null || login != null;
    }

    private void fillBaseInfo(SysOperLog logEntity, HttpServletRequest request) {
        if (request == null) {
            return;
        }
        logEntity.setOperUrl(request.getRequestURI());
        logEntity.setRequestMethod(request.getMethod());
        logEntity.setOperIp(IpUtils.getClientIp(request));
    }

    private void fillOperatorInfo(SysOperLog logEntity) {
        AuthUser user = AuthContext.get();
        if (user == null) {
            return;
        }
        logEntity.setUserId(user.getId());
        logEntity.setUserName(user.getUserName());
        logEntity.setDeptId(user.getDeptId());
        if (StringUtils.isNotBlank(user.getDeptName())) {
            logEntity.setDeptName(user.getDeptName());
        } else if (user.getDeptId() != null && deptService != null) {
            Dept dept = deptService.getById(user.getDeptId());
            if (dept != null && StringUtils.isNotBlank(dept.getName())) {
                logEntity.setDeptName(dept.getName());
            }
        }
    }

    private void fillRequestInfo(SysOperLog logEntity,
                                 ProceedingJoinPoint joinPoint,
                                 HttpServletRequest request,
                                 OperLog operLog) {
        boolean saveParam = operLog == null || operLog.saveParam();
        if (!saveParam) {
            return;
        }
        String[] excludeParams = operLog == null ? DEFAULT_EXCLUDE_PARAMS : operLog.excludeParams();
        String paramJson;
        if (request != null && "GET".equalsIgnoreCase(request.getMethod())) {
            paramJson = toJson(request.getParameterMap());
        } else {
            paramJson = toJson(filterArgs(joinPoint.getArgs()));
        }
        logEntity.setOperParam(truncate(maskSensitive(paramJson, excludeParams)));
    }

    private void fillBizInfo(SysOperLog logEntity,
                             OperLog operLog,
                             RequirePermission permission,
                             String httpMethod) {
        logEntity.setBusinessType(resolveBusinessType(operLog, httpMethod));
        String title = resolveTitle(operLog, permission);
        if (StringUtils.isBlank(title)) {
            title = logEntity.getOperUrl();
        }
        logEntity.setTitle(title);
        if (StringUtils.isBlank(logEntity.getOperation())) {
            String operation = resolveOperation(operLog, permission, httpMethod, logEntity.getOperUrl());
            logEntity.setOperation(operation);
        }
    }

    private Integer resolveBusinessType(OperLog operLog, String httpMethod) {
        if (operLog != null) {
            return operLog.businessType().getCode();
        }
        if ("POST".equalsIgnoreCase(httpMethod)) {
            return BusinessType.INSERT.getCode();
        }
        if ("PUT".equalsIgnoreCase(httpMethod) || "PATCH".equalsIgnoreCase(httpMethod)) {
            return BusinessType.UPDATE.getCode();
        }
        if ("DELETE".equalsIgnoreCase(httpMethod)) {
            return BusinessType.DELETE.getCode();
        }
        return BusinessType.OTHER.getCode();
    }

    private String resolveTitle(OperLog operLog, RequirePermission permission) {
        if (operLog != null && StringUtils.isNotBlank(operLog.title())) {
            return operLog.title();
        }
        String perm = firstPermission(permission);
        if (StringUtils.isBlank(perm)) {
            return null;
        }
        String prefix = perm.split(":")[0];
        return TITLE_MAPPINGS.getOrDefault(prefix, perm);
    }

    private String resolveOperation(OperLog operLog, RequirePermission permission, String method, String url) {
        if (operLog != null && StringUtils.isNotBlank(operLog.operation())) {
            return operLog.operation();
        }
        String perm = firstPermission(permission);
        if (StringUtils.isNotBlank(perm)) {
            return perm;
        }
        if (StringUtils.isNotBlank(method) && StringUtils.isNotBlank(url)) {
            return method + " " + url;
        }
        return method;
    }

    private String resolveSpel(String template, MethodSignature signature, Object[] args, Object result) {
        if (StringUtils.isBlank(template) || !template.contains("#{")) {
            return template;
        }
        try {
            EvaluationContext context = new StandardEvaluationContext();
            String[] paramNames = signature.getParameterNames();
            if (paramNames != null && args != null) {
                for (int i = 0; i < paramNames.length && i < args.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }
            context.setVariable("result", result);
            Matcher matcher = SPEL_PATTERN.matcher(template);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String expression = matcher.group(1);
                Object value = spelParser.parseExpression(expression).getValue(context);
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(value == null ? "null" : String.valueOf(value)));
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        } catch (Exception e) {
            log.warn("解析操作日志SpEL失败: {}", template, e);
            return template;
        }
    }

    private List<Object> filterArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return Collections.emptyList();
        }
        List<Object> results = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                continue;
            }
            String className = arg.getClass().getName();
            if (className.startsWith("org.springframework.validation.")
                    || className.startsWith("org.springframework.web.multipart.")) {
                continue;
            }
            results.add(arg);
        }
        return results;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String maskSensitive(String json, String[] excludeParams) {
        if (StringUtils.isBlank(json) || excludeParams == null || excludeParams.length == 0) {
            return json;
        }
        String masked = json;
        for (String field : excludeParams) {
            if (StringUtils.isBlank(field)) {
                continue;
            }
            String pattern = "\"" + Pattern.quote(field) + "\"\\s*:\\s*\"[^\"]*\"";
            masked = masked.replaceAll(pattern, "\"" + field + "\":\"******\"");
        }
        return masked;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= MAX_TEXT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_TEXT_LENGTH);
    }

    private RequirePermission resolvePermission(Method method, Class<?> targetClass) {
        RequirePermission permission = AnnotatedElementUtils.findMergedAnnotation(method, RequirePermission.class);
        if (permission != null) {
            return permission;
        }
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, RequirePermission.class);
    }

    private RequireLogin resolveLogin(Method method, Class<?> targetClass) {
        RequireLogin login = AnnotatedElementUtils.findMergedAnnotation(method, RequireLogin.class);
        if (login != null) {
            return login;
        }
        return AnnotatedElementUtils.findMergedAnnotation(targetClass, RequireLogin.class);
    }

    private String firstPermission(RequirePermission permission) {
        if (permission == null || permission.value() == null) {
            return null;
        }
        for (String value : permission.value()) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
