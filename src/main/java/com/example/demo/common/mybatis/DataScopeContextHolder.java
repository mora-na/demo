package com.example.demo.common.mybatis;

/**
 * 数据范围上下文持有器，供注解切面与拦截器传递 scopeKey 等信息。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<DataScopeRequest> CONTEXT = new ThreadLocal<>();

    private DataScopeContextHolder() {
    }

    public static void set(DataScopeRequest request) {
        CONTEXT.set(request);
    }

    public static DataScopeRequest get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 数据范围请求上下文。
     */
    public static final class DataScopeRequest {
        private final String scopeKey;
        private final String deptAlias;
        private final String userAlias;

        public DataScopeRequest(String scopeKey, String deptAlias, String userAlias) {
            this.scopeKey = scopeKey;
            this.deptAlias = deptAlias;
            this.userAlias = userAlias;
        }

        public String getScopeKey() {
            return scopeKey;
        }

        public String getDeptAlias() {
            return deptAlias;
        }

        public String getUserAlias() {
            return userAlias;
        }
    }
}
