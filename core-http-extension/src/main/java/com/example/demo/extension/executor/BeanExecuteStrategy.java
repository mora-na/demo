package com.example.demo.extension.executor;

import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.BeanExecuteConfig;
import com.example.demo.extension.model.DynamicApiType;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.example.demo.extension.support.DynamicApiRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean 执行策略。
 */
@Component
public class BeanExecuteStrategy implements ExecuteStrategy {

    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;
    private final DynamicApiConstants constants;

    public BeanExecuteStrategy(ApplicationContext applicationContext,
                               ObjectMapper objectMapper,
                               DynamicApiConstants constants) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
        this.constants = constants;
    }

    @Override
    public DynamicApiType type() {
        return DynamicApiType.BEAN;
    }

    @Override
    public DynamicApiExecuteResult execute(DynamicApiContext context) {
        DynamicApiMeta meta = context.getMeta();
        Object configObj = meta.getConfig();
        if (!(configObj instanceof BeanExecuteConfig)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        BeanExecuteConfig config = (BeanExecuteConfig) configObj;
        String beanName = StringUtils.trimToNull(config.getBeanName());
        String methodName = StringUtils.trimToNull(config.getMethod());
        if (beanName == null || methodName == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        Object bean = applicationContext.getBean(beanName);
        if (bean == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        Method method = resolveMethod(bean.getClass(), methodName, context.getRequest());
        if (method == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        try {
            Object result;
            if (method.getParameterCount() == 0) {
                result = method.invoke(bean);
            } else {
                Object arg = buildArgument(method.getParameterTypes()[0], context.getRequest());
                result = method.invoke(bean, arg);
            }
            return DynamicApiExecuteResult.success(result);
        } catch (Exception ex) {
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed());
        }
    }

    private Method resolveMethod(Class<?> beanClass, String methodName, DynamicApiRequest request) {
        Method[] methods = beanClass.getMethods();
        List<Method> candidates = new ArrayList<>();
        for (Method method : methods) {
            if (!methodName.equals(method.getName())) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            if (method.getParameterCount() > 1) {
                continue;
            }
            candidates.add(method);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        if (candidates.size() == 1) {
            return candidates.get(0);
        }
        // 优先匹配常用签名
        Method match = findByParamType(candidates, DynamicApiRequest.class);
        if (match != null) {
            return match;
        }
        match = findByParamType(candidates, Map.class);
        if (match != null) {
            return match;
        }
        match = findByParamType(candidates, HttpServletRequest.class);
        if (match != null) {
            return match;
        }
        match = findByParamType(candidates, String.class);
        if (match != null) {
            return match;
        }
        return findByParamCount(candidates, 0);
    }

    private Method findByParamType(List<Method> candidates, Class<?> type) {
        for (Method method : candidates) {
            if (method.getParameterCount() == 1 && type.isAssignableFrom(method.getParameterTypes()[0])) {
                return method;
            }
        }
        return null;
    }

    private Method findByParamCount(List<Method> candidates, int count) {
        for (Method method : candidates) {
            if (method.getParameterCount() == count) {
                return method;
            }
        }
        return null;
    }

    private Object buildArgument(Class<?> paramType, DynamicApiRequest request) {
        if (paramType == null) {
            return null;
        }
        if (DynamicApiRequest.class.isAssignableFrom(paramType)) {
            return request;
        }
        if (HttpServletRequest.class.isAssignableFrom(paramType)) {
            return request == null ? null : request.getRawRequest();
        }
        if (Map.class.isAssignableFrom(paramType)) {
            return request == null ? null : request.getParams();
        }
        if (String.class.isAssignableFrom(paramType)) {
            return request == null ? null : request.getRawBody();
        }
        Object body = request == null ? null : request.getBody();
        if (body != null) {
            if (paramType.isInstance(body)) {
                return body;
            }
            try {
                return objectMapper.convertValue(body, paramType);
            } catch (Exception ignored) {
                return body;
            }
        }
        String raw = request == null ? null : request.getRawBody();
        if (raw != null) {
            try {
                return objectMapper.readValue(raw, paramType);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
