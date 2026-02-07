package com.example.demo.framework.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> {

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;

    public static final String SUCCESS_MESSAGE = "success";
    public static final String ERROR_MESSAGE = "error";

    private int code;
    private String message;
    private T data;

    public CommonResult(int code, String message) {
        this(code, message, null);
    }

    public static <T> CommonResult<T> success() {
        return new CommonResult<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> CommonResult<T> success(String message) {
        return new CommonResult<>(SUCCESS_CODE, message, null);
    }

    public static <T> CommonResult<T> success(String message, T data) {
        return new CommonResult<>(SUCCESS_CODE, message, data);
    }

    public static <T> CommonResult<T> error() {
        return new CommonResult<>(ERROR_CODE, ERROR_MESSAGE, null);
    }

    public static <T> CommonResult<T> error(String message) {
        return new CommonResult<>(ERROR_CODE, message, null);
    }

    public static <T> CommonResult<T> error(int code, String message) {
        return new CommonResult<>(code, message, null);
    }

    public static <T> CommonResult<T> error(int code, String message, T data) {
        return new CommonResult<>(code, message, data);
    }
}
