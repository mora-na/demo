package com.example.demo.extension.support;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

import java.util.Map;

/**
 * 动态接口执行线程上下文透传。
 */
public class DynamicApiTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        AuthUser user = AuthContext.get();
        return () -> {
            Map<String, String> previousMdc = MDC.getCopyOfContextMap();
            AuthUser previousUser = AuthContext.get();
            try {
                if (mdc == null || mdc.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(mdc);
                }
                AuthContext.set(user);
                runnable.run();
            } finally {
                if (previousMdc == null || previousMdc.isEmpty()) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(previousMdc);
                }
                if (previousUser == null) {
                    AuthContext.clear();
                } else {
                    AuthContext.set(previousUser);
                }
            }
        };
    }
}
