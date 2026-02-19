package com.example.demo.extension.support;

/**
 * 动态接口异常。
 */
public class DynamicApiException extends RuntimeException {

    private final int code;
    private final String messageKey;
    private final Object details;

    public DynamicApiException(int code, String messageKey) {
        this(code, messageKey, null);
    }

    public DynamicApiException(int code, String messageKey, Object details) {
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
