package com.example.demo.extension.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.extension.adapter.RateLimitAdapter;
import com.example.demo.extension.adapter.RateLimitDecision;
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
import com.example.demo.log.api.event.DynamicApiLogEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

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
    private final ApplicationEventPublisher eventPublisher;
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
            publishLog(match, apiRequest, null, response.getDurationMs(), request, "unauthorized");
            return immediate(response, resolveTimeout(null), 401);
        }
        RateLimitDecision decision = rateLimitAdapter.tryAcquire(request, match.getMeta().getApi().getRateLimitPolicy());
        if (!decision.isAllowed()) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getRateLimitCode(),
                    decision.getMessageKey(),
                    start);
            publishLog(match, apiRequest, null, response.getDurationMs(), request, "rate_limit");
            return immediate(response, resolveTimeout(null), constants.getController().getRateLimitCode());
        }
        long timeoutMs = resolveTimeout(match.getMeta().getApi().getTimeoutMs());
        DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> result = new DeferredResult<>(timeoutMs);
        DynamicApiContext context = new DynamicApiContext(match.getMeta(), apiRequest, timeoutMs);
        final long finalStart = start;
        DynamicApiExecution execution = executor.submit(context);
        java.util.concurrent.atomic.AtomicBoolean timeoutHandled = new java.util.concurrent.atomic.AtomicBoolean(false);
        java.util.concurrent.atomic.AtomicBoolean cancelHandled = new java.util.concurrent.atomic.AtomicBoolean(false);
        execution.getFuture().whenComplete((executeResult, throwable) -> {
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
            ResponseEntity<DynamicApiResponse<Object>> response = buildResponseEntity(request, payload, finalStart);
            publishLog(match, apiRequest, payload, response.getBody() == null ? null : response.getBody().getDurationMs(), request, null);
            result.setResult(response);
        });
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
            publishLog(match, apiRequest, timeoutResult, response.getBody() == null ? null : response.getBody().getDurationMs(),
                    request, "timeout");
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
        return new DynamicApiResponse<>(result.getCode(), message, result.getData(), traceId, cost);
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
        return new DynamicApiResponse<>(code, message, null, traceId, cost);
    }

    private void publishLog(DynamicApiMatch match,
                            DynamicApiRequest apiRequest,
                            DynamicApiExecuteResult result,
                            Long durationMs,
                            HttpServletRequest request,
                            String errorOverride) {
        if (eventPublisher == null || match == null || match.getMeta() == null) {
            return;
        }
        try {
            String traceId = MDC.get(commonConstants.getTrace().getMdcKey());
            String ip = resolveClientIp(request);
            String param = resolveParam(apiRequest);
            boolean success = result != null && result.isSuccess();
            int code = result == null ? constants.getController().getInternalServerErrorCode() : result.getCode();
            String errorMsg = success ? null : (errorOverride != null ? errorOverride : result == null ? null : result.getMessage());
            DynamicApiLogEvent event = new DynamicApiLogEvent(
                    match.getMeta().getApi().getId(),
                    match.getMeta().getApi().getPath(),
                    match.getMeta().getApi().getMethod(),
                    match.getMeta().getApi().getType(),
                    match.getMeta().getApi().getAuthMode(),
                    success ? 1 : 0,
                    code,
                    errorMsg,
                    traceId,
                    AuthContext.get() == null ? null : AuthContext.get().getId(),
                    AuthContext.get() == null ? null : AuthContext.get().getUserName(),
                    ip,
                    param,
                    durationMs,
                    LocalDateTime.now()
            );
            eventPublisher.publishEvent(event);
        } catch (Exception ex) {
            log.warn("Publish dynamic api log failed", ex);
        }
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

    private String resolveParam(DynamicApiRequest apiRequest) {
        if (apiRequest == null) {
            return null;
        }
        Object body = apiRequest.getParams();
        if (body == null) {
            return maskRawBody(apiRequest.getRawBody());
        }
        try {
            Object masked = maskSensitive(body);
            String json = objectMapper.writeValueAsString(masked);
            if (json.length() > constants.getExecute().getLogMaxLength()) {
                return json.substring(0, constants.getExecute().getLogMaxLength()) + "...";
            }
            return json;
        } catch (Exception ex) {
            String raw = String.valueOf(body);
            if (raw.length() > constants.getExecute().getLogMaxLength()) {
                return raw.substring(0, constants.getExecute().getLogMaxLength()) + "...";
            }
            return raw;
        }
    }

    private String maskRawBody(String rawBody) {
        if (rawBody == null) {
            return null;
        }
        try {
            Object parsed = objectMapper.readValue(rawBody, Object.class);
            Object masked = maskSensitive(parsed);
            String json = objectMapper.writeValueAsString(masked);
            if (json.length() > constants.getExecute().getLogMaxLength()) {
                return json.substring(0, constants.getExecute().getLogMaxLength()) + "...";
            }
            return json;
        } catch (Exception ex) {
            if (rawBody.length() > constants.getExecute().getLogMaxLength()) {
                return rawBody.substring(0, constants.getExecute().getLogMaxLength()) + "...";
            }
            return rawBody;
        }
    }

    private Object maskSensitive(Object value) {
        if (value == null) {
            return null;
        }
        Set<String> maskedKeys = buildMaskedKeys();
        return maskValue(value, maskedKeys);
    }

    private Object maskValue(Object value, Set<String> maskedKeys) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map) {
            return maskMap((Map<?, ?>) value, maskedKeys);
        }
        if (value instanceof Iterable) {
            List<Object> list = new ArrayList<>();
            for (Object item : (Iterable<?>) value) {
                list.add(maskValue(item, maskedKeys));
            }
            return list;
        }
        if (value instanceof String) {
            return value;
        }
        try {
            Map<String, Object> map = objectMapper.convertValue(value, new TypeReference<Map<String, Object>>() {
            });
            return maskMap(map, maskedKeys);
        } catch (Exception ex) {
            return value;
        }
    }

    private Map<String, Object> maskMap(Map<?, ?> input, Set<String> maskedKeys) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (input == null) {
            return result;
        }
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = entry.getKey() == null ? null : String.valueOf(entry.getKey());
            if (key == null) {
                continue;
            }
            if (maskedKeys.contains(key.toLowerCase(Locale.ROOT))) {
                result.put(key, "***");
            } else {
                result.put(key, maskValue(entry.getValue(), maskedKeys));
            }
        }
        return result;
    }

    private Set<String> buildMaskedKeys() {
        Set<String> result = new HashSet<>();
        if (constants == null || constants.getExecute() == null || constants.getExecute().getMaskedKeys() == null) {
            return result;
        }
        for (String key : constants.getExecute().getMaskedKeys()) {
            if (key != null && !key.trim().isEmpty()) {
                result.add(key.trim().toLowerCase(Locale.ROOT));
            }
        }
        return result;
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

    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader(commonConstants.getHttp().getForwardedForHeader());
        if (StringUtils.isNotBlank(forwarded)) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        String realIp = request.getHeader(commonConstants.getHttp().getRealIpHeader());
        if (StringUtils.isNotBlank(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
