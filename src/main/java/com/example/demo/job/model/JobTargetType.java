package com.example.demo.job.model;

import java.util.Arrays;
import java.util.List;

/**
 * 定时任务目标范围类型。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public final class JobTargetType {

    public static final String ALL = "ALL";
    public static final String DEPT = "DEPT";
    public static final String ROLE = "ROLE";
    public static final String USER = "USER";

    private static final List<String> SUPPORTED = Arrays.asList(ALL, DEPT, ROLE, USER);

    private JobTargetType() {
    }

    public static boolean isSupported(String value) {
        if (value == null) {
            return false;
        }
        return SUPPORTED.contains(value.trim().toUpperCase());
    }
}
