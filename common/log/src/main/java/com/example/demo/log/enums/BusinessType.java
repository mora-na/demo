package com.example.demo.log.enums;

/**
 * 操作日志业务类型。
 */
public enum BusinessType {
    OTHER(0),
    INSERT(1),
    UPDATE(2),
    DELETE(3),
    GRANT(4),
    EXPORT(5),
    IMPORT(6),
    FORCE_LOGOUT(7),
    CLEAN(8);

    private final int code;

    BusinessType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
