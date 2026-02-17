package com.example.demo.extension.api.exception;

/**
 * 动态接口对外异常。
 */
public class DynamicApiFacadeException extends RuntimeException {

    private final int code;
    private final String messageKey;

    public DynamicApiFacadeException(int code, String messageKey) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
    }

    public int getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
