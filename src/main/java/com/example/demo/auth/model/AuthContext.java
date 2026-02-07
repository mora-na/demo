package com.example.demo.auth.model;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> CONTEXT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser user) {
        CONTEXT.set(user);
    }

    public static AuthUser get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
