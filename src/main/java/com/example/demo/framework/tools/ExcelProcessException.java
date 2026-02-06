package com.example.demo.framework.tools;

/**
 * 统一的Excel处理异常，便于在控制层统一返回。
 */
public class ExcelProcessException extends RuntimeException {

    public ExcelProcessException(String message) {
        super(message);
    }

    public ExcelProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
