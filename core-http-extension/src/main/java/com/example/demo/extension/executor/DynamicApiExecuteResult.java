package com.example.demo.extension.executor;

import com.example.demo.common.model.CommonResult;
import lombok.Getter;

/**
 * 动态接口执行结果。
 */
@Getter
public class DynamicApiExecuteResult {

    private final boolean success;
    private final int code;
    private final String message;
    private final Object data;

    private DynamicApiExecuteResult(boolean success, int code, String message, Object data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static DynamicApiExecuteResult success(Object data) {
        return new DynamicApiExecuteResult(true, CommonResult.SUCCESS_CODE, "common.success", data);
    }

    public static DynamicApiExecuteResult error(int code, String message) {
        return new DynamicApiExecuteResult(false, code, message, null);
    }
}
