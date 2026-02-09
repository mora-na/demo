package com.example.demo.auth.model;

/**
 * 认证用户上下文，使用 ThreadLocal 保存当前请求的用户摘要。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
public final class AuthContext {

    private static final ThreadLocal<AuthUser> CONTEXT = new ThreadLocal<>();

    /**
     * 私有构造函数，禁止实例化。
     */
    private AuthContext() {
    }

    /**
     * 设置当前线程的认证用户。
     *
     * @param user 当前认证用户，允许为 null
     */
    public static void set(AuthUser user) {
        CONTEXT.set(user);
    }

    /**
     * 获取当前线程的认证用户。
     *
     * @return 当前认证用户，未设置时返回 null
     */
    public static AuthUser get() {
        return CONTEXT.get();
    }

    /**
     * 清理当前线程的认证用户，避免线程复用污染。
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
