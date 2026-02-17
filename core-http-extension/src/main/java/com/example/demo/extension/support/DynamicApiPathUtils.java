package com.example.demo.extension.support;

/**
 * 动态接口路径工具。
 */
public final class DynamicApiPathUtils {

    private DynamicApiPathUtils() {
    }

    public static boolean isPatternPath(String path) {
        if (path == null) {
            return false;
        }
        return path.contains("{") || path.contains("*") || path.contains("?");
    }
}
