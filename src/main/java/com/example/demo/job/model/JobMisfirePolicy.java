package com.example.demo.job.model;

import java.util.Arrays;
import java.util.List;

/**
 * 定时任务误触发策略常量。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public final class JobMisfirePolicy {

    public static final String DEFAULT = "DEFAULT";
    public static final String IGNORE_MISFIRE = "IGNORE_MISFIRE";
    public static final String FIRE_AND_PROCEED = "FIRE_AND_PROCEED";
    public static final String DO_NOTHING = "DO_NOTHING";

    private static final List<String> SUPPORTED = Arrays.asList(
            DEFAULT,
            IGNORE_MISFIRE,
            FIRE_AND_PROCEED,
            DO_NOTHING
    );

    private JobMisfirePolicy() {
    }

    public static boolean isSupported(String value) {
        if (value == null) {
            return false;
        }
        return SUPPORTED.contains(value.trim().toUpperCase());
    }
}
