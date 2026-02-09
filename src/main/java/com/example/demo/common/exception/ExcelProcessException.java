package com.example.demo.common.exception;

/**
 * Excel 处理异常，用于统一封装导入导出错误。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public class ExcelProcessException extends RuntimeException {

    /**
     * 构造异常并指定消息。
     *
     * @param message 错误消息
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public ExcelProcessException(String message) {
        super(message);
    }

    /**
     * 构造异常并指定消息与根因。
     *
     * @param message 错误消息
     * @param cause   根因异常
     * @author GPT-5.2-codex(high)
     * @date 2026/2/9
     */
    public ExcelProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
