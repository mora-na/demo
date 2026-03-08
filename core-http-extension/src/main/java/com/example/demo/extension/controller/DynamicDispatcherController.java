package com.example.demo.extension.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.extension.adapter.RateLimitAdapter;
import com.example.demo.extension.adapter.RateLimitDecision;
import com.example.demo.extension.api.executor.DynamicApiContextAttributes;
import com.example.demo.extension.api.executor.DynamicApiExecuteResult;
import com.example.demo.extension.api.executor.DynamicApiTerminationReason;
import com.example.demo.extension.api.request.DynamicApiParamMode;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.executor.DynamicApiContext;
import com.example.demo.extension.executor.DynamicApiExecution;
import com.example.demo.extension.executor.DynamicApiExecutor;
import com.example.demo.extension.model.BeanExecuteConfig;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.model.DynamicApiResponse;
import com.example.demo.extension.model.DynamicApiStatus;
import com.example.demo.extension.registry.DynamicApiMatch;
import com.example.demo.extension.registry.DynamicApiRegistry;
import com.example.demo.extension.support.DynamicApiRequestExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logcollect.core.context.LogCollectContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 动态接口统一入口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DynamicDispatcherController {

    private static final String TERMINATION_HEADER = "X-Dynamic-Api-Termination";

    private final DynamicApiRegistry registry;
    private final DynamicApiExecutor executor;
    private final DynamicApiRequestExtractor requestExtractor;
    private final DynamicApiProperties properties;
    private final DynamicApiConstants constants;
    private final RateLimitAdapter rateLimitAdapter;
    private final I18nService i18nService;
    private final CommonConstants commonConstants;
    private final ObjectMapper objectMapper;

    @RequestMapping("/ext/**")
    public DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> dispatch(HttpServletRequest request) {
        long start = System.currentTimeMillis();
        if (!properties.getGlobal().isEnabled()) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getServiceUnavailableCode(),
                    constants.getMessage().getGlobalDisabled(),
                    start);
            return immediate(response, resolveTimeout(null), constants.getController().getServiceUnavailableCode());
        }
        DynamicApiMatch match = registry.match(request.getMethod(), request.getRequestURI());
        if (match == null || match.getMeta() == null || match.getMeta().getApi() == null) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getNotFoundCode(),
                    constants.getMessage().getNotFound(),
                    start);
            return immediate(response, resolveTimeout(null), constants.getController().getNotFoundCode());
        }
        if (!DynamicApiStatus.ENABLED.name().equalsIgnoreCase(match.getMeta().getApi().getStatus())) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getNotFoundCode(),
                    constants.getMessage().getNotFound(),
                    start);
            return immediate(response, resolveTimeout(null), constants.getController().getNotFoundCode());
        }
        DynamicApiParamMode paramMode = resolveParamMode(match);
        DynamicApiRequest apiRequest = requestExtractor.extract(request, match.getPathVariables(), paramMode);
        if (DynamicApiAuthMode.INHERIT.equals(match.getMeta().getAuthMode()) && AuthContext.get() == null) {
            DynamicApiResponse<Object> response = buildError(request, 401, "auth.permission.required", start);
            return immediate(response, resolveTimeout(null), 401);
        }
        RateLimitDecision decision = rateLimitAdapter.tryAcquire(request, match.getMeta().getApi().getRateLimitPolicy());
        if (!decision.isAllowed()) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getRateLimitCode(),
                    decision.getMessageKey(),
                    start);
            return immediate(response, resolveTimeout(null), constants.getController().getRateLimitCode());
        }
        long timeoutMs = resolveTimeout(match.getMeta().getApi().getTimeoutMs());
        DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> result = new DeferredResult<>(timeoutMs);
        String traceId = MDC.get(commonConstants.getTrace().getMdcKey());
        if (StringUtils.isBlank(traceId)) {
            traceId = resolveHeader(apiRequest, constants.getHttp().getTraceIdHeader());
        }
        String requestId = resolveHeader(apiRequest, constants.getHttp().getRequestIdHeader());
        String tenantId = resolveHeader(apiRequest, constants.getHttp().getTenantIdHeader());
        Map<String, Object> attributes = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(traceId)) {
            attributes.put(DynamicApiContextAttributes.TRACE_ID, traceId);
        }
        if (StringUtils.isNotBlank(requestId)) {
            attributes.put(DynamicApiContextAttributes.REQUEST_ID, requestId);
        }
        if (StringUtils.isNotBlank(tenantId)) {
            attributes.put(DynamicApiContextAttributes.TENANT_ID, tenantId);
        }
        DynamicApiContext context = new DynamicApiContext(match.getMeta(), apiRequest, timeoutMs,
                traceId, requestId, tenantId, attributes);
        final long finalStart = start;
        DynamicApiExecution execution = executor.submit(context);
        java.util.concurrent.atomic.AtomicBoolean timeoutHandled = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicBoolean cancelHandled = new java.util.concurrent.atomic.AtomicBoolean(false);
        BiConsumer<DynamicApiExecuteResult, Throwable> completionCallback =
                LogCollectContextUtils.wrapBiConsumer((executeResult, throwable) -> {
            if (timeoutHandled.get() || result.isSetOrExpired()) {
                return;
            }
            if (throwable != null) {
                log.error("Dynamic api execute async error: apiId={}, path={}, method={}, type={}, traceId={}",
                        match.getMeta().getApi().getId(),
                        match.getMeta().getApi().getPath(),
                        match.getMeta().getApi().getMethod(),
                        match.getMeta().getType(),
                        MDC.get(commonConstants.getTrace().getMdcKey()),
                        throwable);
            }
            DynamicApiExecuteResult payload = executeResult == null
                    ? DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed(),
                    DynamicApiTerminationReason.ERROR)
                    : executeResult;
            payload = enforceResponseSize(payload);
            ResponseEntity<DynamicApiResponse<Object>> response = buildResponseEntity(request, payload, finalStart);
            result.setResult(response);
        });
        execution.getFuture().whenComplete(completionCallback);
        result.onError(throwable -> {
            if (cancelHandled.compareAndSet(false, true) && !execution.getFuture().isDone()) {
                execution.cancel(DynamicApiTerminationReason.CANCELLED, throwable);
            }
        });
        result.onCompletion(() -> {
            if (cancelHandled.compareAndSet(false, true) && !execution.getFuture().isDone()) {
                execution.cancel(DynamicApiTerminationReason.CANCELLED, null);
            }
        });
        result.onTimeout(() -> {
            timeoutHandled.set(true);
            execution.cancelTimeout();
            DynamicApiExecuteResult timeoutResult = DynamicApiExecuteResult.error(
                    constants.getController().getServiceUnavailableCode(),
                    constants.getMessage().getTimeout(),
                    DynamicApiTerminationReason.TIMEOUT);
            ResponseEntity<DynamicApiResponse<Object>> response = buildResponseEntity(request, timeoutResult, finalStart);
            if (!result.isSetOrExpired()) {
                result.setResult(response);
            }
        });
        return result;
    }

    private DynamicApiResponse<Object> buildResponse(HttpServletRequest request,
                                                     DynamicApiExecuteResult result,
                                                     long start) {
        long cost = Math.max(0, System.currentTimeMillis() - start);
        String traceId = MDC.get(commonConstants.getTrace().getMdcKey());
        String message = i18nService.getMessage(request, result.getMessage());
        return new DynamicApiResponse<>(result.getCode(), message, result.getData(), traceId, cost,
                result.getErrorDetails(), result.getMeta());
    }

    private ResponseEntity<DynamicApiResponse<Object>> buildResponseEntity(HttpServletRequest request,
                                                                           DynamicApiExecuteResult result,
                                                                           long start) {
        DynamicApiResponse<Object> response = buildResponse(request, result, start);
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(result.getCode());
        if (result.getTerminationReason() != null && !result.getTerminationReason().isEmpty()) {
            builder.header(TERMINATION_HEADER, result.getTerminationReason());
        }
        return builder.body(response);
    }

    private DynamicApiResponse<Object> buildError(HttpServletRequest request,
                                                  int code,
                                                  String messageKey,
                                                  long start) {
        long cost = Math.max(0, System.currentTimeMillis() - start);
        String traceId = MDC.get(commonConstants.getTrace().getMdcKey());
        String message = i18nService.getMessage(request, messageKey);
        return new DynamicApiResponse<>(code, message, null, traceId, cost, null, null);
    }

    private String resolveHeader(DynamicApiRequest request, String headerName) {
        if (request == null || StringUtils.isBlank(headerName)) {
            return null;
        }
        Map<String, String> headers = request.getHeaders();
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(headerName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private DynamicApiExecuteResult enforceResponseSize(DynamicApiExecuteResult payload) {
        if (payload == null || !payload.isSuccess()) {
            return payload;
        }
        long maxBytes = constants.getExecute().getMaxResponseBytes();
        if (maxBytes <= 0) {
            return payload;
        }
        Object data = payload.getData();
        if (data == null) {
            return payload;
        }
        long size;
        if (data instanceof byte[]) {
            size = ((byte[]) data).length;
        } else if (data instanceof CharSequence) {
            size = data.toString().getBytes(StandardCharsets.UTF_8).length;
        } else {
            try {
                size = objectMapper.writeValueAsBytes(data).length;
            } catch (Exception ex) {
                return payload;
            }
        }
        if (size > maxBytes) {
            return DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                    constants.getMessage().getResponseTooLarge(),
                    DynamicApiTerminationReason.ERROR);
        }
        return payload;
    }

    private DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> immediate(DynamicApiResponse<Object> response,
                                                                                 long timeoutMs,
                                                                                 int status) {
        DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> result =
                new DeferredResult<>(Math.max(1, timeoutMs));
        result.setResult(ResponseEntity.status(status).body(response));
        return result;
    }

    private long resolveTimeout(Integer apiTimeout) {
        long timeoutMs = apiTimeout != null && apiTimeout > 0
                ? apiTimeout.longValue()
                : constants.getExecute().getDefaultTimeoutMs();
        long maxTimeoutMs = constants.getExecute().getMaxTimeoutMs();
        if (maxTimeoutMs > 0 && timeoutMs > maxTimeoutMs) {
            timeoutMs = maxTimeoutMs;
        }
        return Math.max(1, timeoutMs);
    }

    private DynamicApiParamMode resolveParamMode(DynamicApiMatch match) {
        if (match == null || match.getMeta() == null) {
            return DynamicApiParamMode.AUTO;
        }
        Object config = match.getMeta().getConfig();
        if (config instanceof BeanExecuteConfig) {
            BeanExecuteConfig beanConfig = (BeanExecuteConfig) config;
            DynamicApiParamMode mode = DynamicApiParamMode.from(beanConfig.getParamMode());
            return mode == null ? DynamicApiParamMode.AUTO : mode;
        }
        return DynamicApiParamMode.AUTO;
    }

}
