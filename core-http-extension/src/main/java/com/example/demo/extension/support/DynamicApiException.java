package com.example.demo.extension.support;

/**
 * 动态接口异常。
 */
public class DynamicApiException extends RuntimeException {

    private final int code;
    private final String messageKey;

    public DynamicApiException(int code, String messageKey) {
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
