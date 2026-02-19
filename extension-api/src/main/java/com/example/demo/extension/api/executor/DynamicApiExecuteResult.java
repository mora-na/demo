package com.example.demo.extension.api.executor;

import com.example.demo.common.model.CommonResult;
import lombok.Getter;

import java.util.Map;

/**
 * 动态接口执行结果。
 */
@Getter
public class DynamicApiExecuteResult {

    private final boolean success;
    private final int code;
    private final String message;
    private final Object data;
    private final String terminationReason;
    private final Object errorDetails;
    private final Map<String, Object> meta;

    private DynamicApiExecuteResult(boolean success,
                                    int code,
                                    String message,
                                    Object data,
                                    String terminationReason,
                                    Object errorDetails,
                                    Map<String, Object> meta) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.terminationReason = terminationReason;
        this.errorDetails = errorDetails;
        this.meta = meta;
    }

    public static DynamicApiExecuteResult success(Object data) {
        return success(data, null);
    }

    public static DynamicApiExecuteResult success(Object data, Map<String, Object> meta) {
        return new DynamicApiExecuteResult(true, CommonResult.SUCCESS_CODE, "common.success", data, null, null, meta);
    }

    public static DynamicApiExecuteResult error(int code, String message) {
        return error(code, message, null, null, null);
    }

    public static DynamicApiExecuteResult error(int code, String message, DynamicApiTerminationReason reason) {
        return error(code, message, reason, null, null);
    }

    public static DynamicApiExecuteResult error(int code,
                                                String message,
                                                DynamicApiTerminationReason reason,
                                                Object errorDetails) {
        return error(code, message, reason, errorDetails, null);
    }

    public static DynamicApiExecuteResult error(int code,
                                                String message,
                                                DynamicApiTerminationReason reason,
                                                Object errorDetails,
                                                Map<String, Object> meta) {
        return new DynamicApiExecuteResult(false, code, message, null,
                reason == null ? null : reason.name(), errorDetails, meta);
    }
}
