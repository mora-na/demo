package com.example.demo.extension.api.exception;

/**
 * 动态接口对外异常。
 */
public class DynamicApiFacadeException extends RuntimeException {

    private final int code;
    private final String messageKey;
    private final Object details;

    public DynamicApiFacadeException(int code, String messageKey) {
        this(code, messageKey, null);
    }

    public DynamicApiFacadeException(int code, String messageKey, Object details) {
        super(messageKey);
        this.code = code;
        this.messageKey = messageKey;
        this.details = details;
    }

    public int getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object getDetails() {
        return details;
    }
}
