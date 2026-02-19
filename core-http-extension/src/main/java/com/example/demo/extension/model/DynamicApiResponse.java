package com.example.demo.extension.model;

import com.example.demo.common.model.CommonResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 动态接口统一响应。
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DynamicApiResponse<T> {

    private int code;
    private String message;
    private T data;
    private String traceId;
    private Long durationMs;
    private Object errorDetails;
    private java.util.Map<String, Object> meta;

    public static <T> DynamicApiResponse<T> success(T data, String traceId, Long durationMs) {
        return new DynamicApiResponse<>(CommonResult.SUCCESS_CODE, CommonResult.SUCCESS_MESSAGE, data, traceId, durationMs, null, null);
    }

    public static <T> DynamicApiResponse<T> error(int code, String message, String traceId, Long durationMs) {
        return new DynamicApiResponse<>(code, message, null, traceId, durationMs, null, null);
    }
}
