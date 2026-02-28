package com.example.demo.common.mybatis;

/**
 * 数据范围类型常量定义。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class DataScopeType {

    /**
     * 全部数据范围。
     *
     * @date 2026/2/9
     */
    public static final String ALL = "ALL";
    /**
     * 本部门及下级部门数据范围。
     *
     * @date 2026/2/9
     */
    public static final String DEPT_AND_CHILD = "DEPT_AND_CHILD";
    /**
     * 本部门数据范围。
     *
     * @date 2026/2/9
     */
    public static final String DEPT = "DEPT";
    /**
     * 自定义部门数据范围。
     *
     * @date 2026/2/9
     */
    public static final String CUSTOM_DEPT = "CUSTOM_DEPT";
    /**
     * 仅本人数据范围。
     *
     * @date 2026/2/9
     */
    public static final String SELF = "SELF";
    /**
     * 自定义数据范围。
     *
     * @date 2026/2/9
     */
    public static final String CUSTOM = "CUSTOM";
    /**
     * 无可见数据范围。
     *
     * @date 2026/2/9
     */
    public static final String NONE = "NONE";

    /**
     * 私有构造函数，禁止实例化。
     */
    private DataScopeType() {
    }
}
