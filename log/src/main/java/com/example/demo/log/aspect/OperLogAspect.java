package com.example.demo.log.aspect;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.web.permission.RequireLogin;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.identity.api.facade.OrgApi;
import com.example.demo.log.annotation.OperLog;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.entity.SysOperLog;
import com.example.demo.log.enums.BusinessType;
import com.example.demo.log.event.OperLogEvent;
import com.example.demo.log.support.IpUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.BridgeMethodResolver;
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

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final OrgApi orgApi;
    private final LogConstants logConstants;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public OperLogAspect(ApplicationEventPublisher eventPublisher,
                         ObjectMapper objectMapper,
                         OrgApi orgApi,
                         LogConstants logConstants) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.orgApi = orgApi;
        this.logConstants = logConstants;
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
        Method specificMethod = resolveSpecificMethod(method, targetClass);
        OperLog operLog = AnnotatedElementUtils.findMergedAnnotation(specificMethod, OperLog.class);
        RequirePermission permission = resolvePermission(specificMethod, targetClass);
        RequireLogin login = resolveLogin(specificMethod, targetClass);
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
            logEntity.setStatus(logConstants.getStatus().getOperSuccess());
            if (operLog != null && operLog.saveResult() && result != null) {
                logEntity.setOperResult(truncate(maskSensitive(toJson(result), resolveExcludeParams(operLog))));
            }
            return result;
        } catch (Throwable ex) {
            error = ex;
            logEntity.setStatus(logConstants.getStatus().getOperFailed());
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
        if (Strings.CI.equalsAny(httpMethod,
                logConstants.getHttp().getGetMethod(),
                logConstants.getHttp().getOptionsMethod())) {
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
        } else if (user.getDeptId() != null && orgApi != null) {
            String deptName = orgApi.getDeptNameById(user.getDeptId());
            if (StringUtils.isNotBlank(deptName)) {
                logEntity.setDeptName(deptName);
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
        String[] excludeParams = resolveExcludeParams(operLog);
        String paramJson;
        if (request != null && Strings.CI.equals(request.getMethod(), logConstants.getHttp().getGetMethod())) {
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
        if (Strings.CI.equals(httpMethod, logConstants.getHttp().getPostMethod())) {
            return BusinessType.INSERT.getCode();
        }
        if (Strings.CI.equalsAny(httpMethod,
                logConstants.getHttp().getPutMethod(),
                logConstants.getHttp().getPatchMethod())) {
            return BusinessType.UPDATE.getCode();
        }
        if (Strings.CI.equals(httpMethod, logConstants.getHttp().getDeleteMethod())) {
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
        String prefix = permissionPrefix(perm);
        Map<String, String> mappings = logConstants.getAspect().getTitleMappings();
        if (mappings == null || mappings.isEmpty()) {
            return perm;
        }
        return mappings.getOrDefault(prefix, perm);
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
            return method + logConstants.getHttp().getMethodUrlSeparator() + url;
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
            Matcher matcher = resolveSpelPattern().matcher(template);
            StringBuffer buffer = new StringBuffer();
            while (matcher.find()) {
                String expression = matcher.group(1);
                Object value = spelParser.parseExpression(expression).getValue(context);
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(
                        value == null ? logConstants.getAspect().getSpelNullLiteral() : String.valueOf(value)));
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        } catch (Exception e) {
            log.warn(logConstants.getMessage().getSpelParseFailed(), template, e);
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
            if (className.startsWith(logConstants.getAspect().getSpringValidationPackagePrefix())
                    || className.startsWith(logConstants.getAspect().getSpringMultipartPackagePrefix())) {
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
        Set<String> fields = new HashSet<>();
        for (String field : excludeParams) {
            if (StringUtils.isNotBlank(field)) {
                fields.add(field);
            }
        }
        if (fields.isEmpty()) {
            return json;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root == null) {
                return json;
            }
            boolean changed = maskNode(root, fields);
            if (!changed) {
                return json;
            }
            return objectMapper.writeValueAsString(root);
        } catch (Exception ignored) {
            return maskSensitiveFlat(json, fields);
        }
    }

    private boolean maskNode(JsonNode node, Set<String> fields) {
        if (node == null) {
            return false;
        }
        boolean changed = false;
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            java.util.Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields();
            while (iterator.hasNext()) {
                Map.Entry<String, JsonNode> entry = iterator.next();
                String name = entry.getKey();
                JsonNode child = entry.getValue();
                if (fields.contains(name)) {
                    objectNode.put(name, logConstants.getAspect().getMaskValue());
                    changed = true;
                } else {
                    changed |= maskNode(child, fields);
                }
            }
            return changed;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                changed |= maskNode(arrayNode.get(i), fields);
            }
        }
        return changed;
    }

    private String maskSensitiveFlat(String json, Set<String> fields) {
        String masked = json;
        for (String field : fields) {
            String pattern = "\"" + Pattern.quote(field) + "\"\\s*:\\s*\"[^\"]*\"";
            masked = masked.replaceAll(pattern, "\"" + field + "\":\"" + logConstants.getAspect().getMaskValue() + "\"");
        }
        return masked;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        if (value.length() <= logConstants.getAspect().getMaxTextLength()) {
            return value;
        }
        return value.substring(0, logConstants.getAspect().getMaxTextLength());
    }

    private String[] resolveExcludeParams(OperLog operLog) {
        if (operLog != null && operLog.excludeParams() != null && operLog.excludeParams().length > 0) {
            return operLog.excludeParams();
        }
        List<String> defaults = logConstants.getAspect().getDefaultExcludeParams();
        if (defaults == null || defaults.isEmpty()) {
            return new String[0];
        }
        return defaults.toArray(new String[0]);
    }

    private Pattern resolveSpelPattern() {
        String regex = logConstants.getAspect().getSpelPattern();
        if (StringUtils.isBlank(regex)) {
            return Pattern.compile(LogConstants.Aspect.DEFAULT_SPEL_PATTERN);
        }
        try {
            return Pattern.compile(regex);
        } catch (Exception ignored) {
            return Pattern.compile(LogConstants.Aspect.DEFAULT_SPEL_PATTERN);
        }
    }

    private String permissionPrefix(String permission) {
        String separator = logConstants.getHttp().getPermissionSeparator();
        if (StringUtils.isBlank(separator)) {
            return permission;
        }
        int idx = permission.indexOf(separator);
        if (idx <= 0) {
            return permission;
        }
        return permission.substring(0, idx);
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

    private Method resolveSpecificMethod(Method method, Class<?> targetClass) {
        if (method == null || targetClass == null) {
            return method;
        }
        Method specific = AopUtils.getMostSpecificMethod(method, targetClass);
        return BridgeMethodResolver.findBridgedMethod(specific);
    }
}
