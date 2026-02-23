package com.example.demo.common.web.permission;

/**
 * 权限逻辑关系枚举，用于多权限匹配的 AND/OR 组合。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public enum Logical {
    /**
     * 逻辑与，所有权限都满足。
     *
     * @date 2026/2/9
     */
    AND,
    /**
     * 逻辑或，任一权限满足即可。
     *
     * @date 2026/2/9
     */
    OR
}
