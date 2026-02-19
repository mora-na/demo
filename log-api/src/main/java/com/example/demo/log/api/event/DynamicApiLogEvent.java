package com.example.demo.log.api.event;

import java.time.LocalDateTime;

/**
 * 动态接口调用日志事件。
 */
public class DynamicApiLogEvent {

    private final Long apiId;
    private final String apiPath;
    private final String apiMethod;
    private final String apiType;
    private final String authMode;
    private final Integer status;
    private final Integer responseCode;
    private final String errorMsg;
    private final String errorDetails;
    private final String meta;
    private final String traceId;
    private final Long userId;
    private final String userName;
    private final String requestIp;
    private final String requestParam;
    private final Long durationMs;
    private final LocalDateTime requestTime;

    public DynamicApiLogEvent(Long apiId,
                              String apiPath,
                              String apiMethod,
                              String apiType,
                              String authMode,
                              Integer status,
                              Integer responseCode,
                              String errorMsg,
                              String errorDetails,
                              String meta,
                              String traceId,
                              Long userId,
                              String userName,
                              String requestIp,
                              String requestParam,
                              Long durationMs,
                              LocalDateTime requestTime) {
        this.apiId = apiId;
        this.apiPath = apiPath;
        this.apiMethod = apiMethod;
        this.apiType = apiType;
        this.authMode = authMode;
        this.status = status;
        this.responseCode = responseCode;
        this.errorMsg = errorMsg;
        this.errorDetails = errorDetails;
        this.meta = meta;
        this.traceId = traceId;
        this.userId = userId;
        this.userName = userName;
        this.requestIp = requestIp;
        this.requestParam = requestParam;
        this.durationMs = durationMs;
        this.requestTime = requestTime;
    }

    public Long getApiId() {
        return apiId;
    }

    public String getApiPath() {
        return apiPath;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public String getApiType() {
        return apiType;
    }

    public String getAuthMode() {
        return authMode;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public String getMeta() {
        return meta;
    }

    public String getTraceId() {
        return traceId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public String getRequestParam() {
        return requestParam;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public LocalDateTime getRequestTime() {
        return requestTime;
    }
}
