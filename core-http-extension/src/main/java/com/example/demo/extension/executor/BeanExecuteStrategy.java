package com.example.demo.extension.executor;

import com.example.demo.extension.api.handler.DynamicApiHandler;
import com.example.demo.extension.api.request.DynamicApiParamMode;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.model.BeanExecuteConfig;
import com.example.demo.extension.model.DynamicApiTypeCodes;
import com.example.demo.extension.support.DynamicApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * Bean 执行策略。
 */
@Slf4j
@Component
public class BeanExecuteStrategy implements ExecuteStrategy {

    private final ApplicationContext applicationContext;
    private final DynamicApiConstants constants;

    public BeanExecuteStrategy(ApplicationContext applicationContext,
                               DynamicApiConstants constants) {
        this.applicationContext = applicationContext;
        this.constants = constants;
    }

    @Override
    public String type() {
        return DynamicApiTypeCodes.BEAN;
    }

    @Override
    public String displayName() {
        return "Bean";
    }

    @Override
    public Object parseConfig(String configJson, ObjectMapper objectMapper) throws Exception {
        if (StringUtils.isBlank(configJson)) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        BeanExecuteConfig config = objectMapper.readValue(configJson, BeanExecuteConfig.class);
        if (config == null || StringUtils.isBlank(config.getBeanName())) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        if (StringUtils.isNotBlank(config.getParamMode())
                && DynamicApiParamMode.from(config.getParamMode()) == null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        return config;
    }

    @Override
    public DynamicApiExecuteResult execute(DynamicApiContext context) {
        Object configObj = context.getMeta().getConfig();
        if (!(configObj instanceof BeanExecuteConfig)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        BeanExecuteConfig config = (BeanExecuteConfig) configObj;
        String beanName = StringUtils.trimToNull(config.getBeanName());
        if (beanName == null) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        Object bean = applicationContext.getBean(beanName);
        if (!(bean instanceof DynamicApiHandler)) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getBeanInvalid());
        }
        try {
            DynamicApiHandler handler = (DynamicApiHandler) bean;
            DynamicApiRequest request = context.getRequest();
            Object result = handler.handle(request);
            return DynamicApiExecuteResult.success(result);
        } catch (Exception ex) {
            String traceId = MDC.get("traceId");
            log.error("Dynamic api bean execute failed: apiId={}, path={}, method={}, bean={}, traceId={}",
                    context.getMeta().getApi().getId(),
                    context.getMeta().getApi().getPath(),
                    context.getMeta().getApi().getMethod(),
                    beanName,
                    traceId,
                    ex);
            return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed());
        }
    }

}
