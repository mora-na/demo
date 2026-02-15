package com.example.demo.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应结果封装，包含状态码、消息与数据体。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
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

    /**
     * 快速构造响应结果（无数据体）。
     *
     * @param code    状态码
     * @param message 提示信息
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public CommonResult(int code, String message) {
        this(code, message, null);
    }

    /**
     * 构造成功响应（无数据体）。
     *
     * @param <T> 数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> success() {
        return new CommonResult<>(SUCCESS_CODE, SUCCESS_MESSAGE, null);
    }

    /**
     * 构造成功响应（带数据体）。
     *
     * @param data 数据体
     * @param <T>  数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<>(SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    /**
     * 构造成功响应（自定义消息）。
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> success(String message) {
        return new CommonResult<>(SUCCESS_CODE, message, null);
    }

    /**
     * 构造成功响应（自定义消息与数据体）。
     *
     * @param message 提示信息
     * @param data    数据体
     * @param <T>     数据类型
     * @return 成功响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> success(String message, T data) {
        return new CommonResult<>(SUCCESS_CODE, message, data);
    }

    /**
     * 构造失败响应（默认消息）。
     *
     * @param <T> 数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> error() {
        return new CommonResult<>(ERROR_CODE, ERROR_MESSAGE, null);
    }

    /**
     * 构造失败响应（自定义消息）。
     *
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> error(String message) {
        return new CommonResult<>(ERROR_CODE, message, null);
    }

    /**
     * 构造失败响应（自定义状态码与消息）。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param <T>     数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> error(int code, String message) {
        return new CommonResult<>(code, message, null);
    }

    /**
     * 构造失败响应（自定义状态码、消息与数据体）。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    数据体
     * @param <T>     数据类型
     * @return 失败响应
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public static <T> CommonResult<T> error(int code, String message, T data) {
        return new CommonResult<>(code, message, data);
    }
}
