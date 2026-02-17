package com.example.demo.extension.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.common.config.CommonConstants;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.extension.adapter.RateLimitAdapter;
import com.example.demo.extension.adapter.RateLimitDecision;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.config.DynamicApiProperties;
import com.example.demo.extension.executor.DynamicApiContext;
import com.example.demo.extension.executor.DynamicApiExecuteResult;
import com.example.demo.extension.executor.DynamicApiExecutor;
import com.example.demo.extension.model.DynamicApiAuthMode;
import com.example.demo.extension.model.DynamicApiResponse;
import com.example.demo.extension.model.DynamicApiStatus;
import com.example.demo.extension.registry.DynamicApiMatch;
import com.example.demo.extension.registry.DynamicApiRegistry;
import com.example.demo.extension.support.DynamicApiRequest;
import com.example.demo.extension.support.DynamicApiRequestExtractor;
import com.example.demo.log.api.event.DynamicApiLogEvent;
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

/**
 * 动态接口统一入口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class DynamicDispatcherController {

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
            return immediate(response, properties.getDefaultTimeoutMs(), constants.getController().getServiceUnavailableCode());
        }
        DynamicApiMatch match = registry.match(request.getMethod(), request.getRequestURI());
        if (match == null || match.getMeta() == null || match.getMeta().getApi() == null) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getNotFoundCode(),
                    constants.getMessage().getNotFound(),
                    start);
            return immediate(response, properties.getDefaultTimeoutMs(), constants.getController().getNotFoundCode());
        }
        if (!DynamicApiStatus.ENABLED.name().equalsIgnoreCase(match.getMeta().getApi().getStatus())) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getNotFoundCode(),
                    constants.getMessage().getNotFound(),
                    start);
            return immediate(response, properties.getDefaultTimeoutMs(), constants.getController().getNotFoundCode());
        }
        DynamicApiRequest apiRequest = requestExtractor.extract(request, match.getPathVariables());
        if (DynamicApiAuthMode.INHERIT.equals(match.getMeta().getAuthMode()) && AuthContext.get() == null) {
            DynamicApiResponse<Object> response = buildError(request, 401, "auth.permission.required", start);
            publishLog(match, apiRequest, null, response.getDurationMs(), request, "unauthorized");
            return immediate(response, properties.getDefaultTimeoutMs(), 401);
        }
        RateLimitDecision decision = rateLimitAdapter.tryAcquire(request, match.getMeta().getApi().getRateLimitPolicy());
        if (!decision.isAllowed()) {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getRateLimitCode(),
                    decision.getMessageKey(),
                    start);
            publishLog(match, apiRequest, null, response.getDurationMs(), request, "rate_limit");
            return immediate(response, properties.getDefaultTimeoutMs(), constants.getController().getRateLimitCode());
        }
        long timeoutMs = resolveTimeout(match.getMeta().getApi().getTimeoutMs());
        DeferredResult<ResponseEntity<DynamicApiResponse<Object>>> result = new DeferredResult<>(timeoutMs);
        DynamicApiContext context = new DynamicApiContext(match.getMeta(), apiRequest, timeoutMs);
        final long finalStart = start;
        executor.executeAsync(context).whenComplete((executeResult, throwable) -> {
            DynamicApiExecuteResult payload = executeResult == null
                    ? DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                    constants.getMessage().getExecuteFailed())
                    : executeResult;
            DynamicApiResponse<Object> response = buildResponse(request, payload, finalStart);
            publishLog(match, apiRequest, payload, response.getDurationMs(), request, null);
            result.setResult(ResponseEntity.status(payload.getCode()).body(response));
        });
        result.onTimeout(() -> {
            DynamicApiResponse<Object> response = buildError(request,
                    constants.getController().getServiceUnavailableCode(),
                    constants.getMessage().getTimeout(),
                    finalStart);
            publishLog(match, apiRequest, null, response.getDurationMs(), request, "timeout");
            result.setResult(ResponseEntity.status(constants.getController().getServiceUnavailableCode()).body(response));
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
        if (apiTimeout != null && apiTimeout > 0) {
            return apiTimeout;
        }
        return Math.max(1, properties.getDefaultTimeoutMs());
    }

    private String resolveParam(DynamicApiRequest apiRequest) {
        if (apiRequest == null) {
            return null;
        }
        Object body = apiRequest.getParams();
        if (body == null) {
            return apiRequest.getRawBody();
        }
        try {
            String json = objectMapper.writeValueAsString(body);
            if (json.length() > constants.getExecute().getLogMaxLength()) {
                return json.substring(0, constants.getExecute().getLogMaxLength());
            }
            return json;
        } catch (Exception ex) {
            String raw = String.valueOf(body);
            if (raw.length() > constants.getExecute().getLogMaxLength()) {
                return raw.substring(0, constants.getExecute().getLogMaxLength());
            }
            return raw;
        }
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
