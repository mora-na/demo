package com.example.demo.notice.model;

import com.example.demo.common.spring.SpringContextHolder;
import com.example.demo.notice.config.NoticeConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.Locale;

/**
 * 通知范围类型常量。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public final class NoticeScopeType {

    public static final String ALL = NoticeConstants.Scope.DEFAULT_ALL;
    public static final String DEPT = NoticeConstants.Scope.DEFAULT_DEPT;
    public static final String ROLE = NoticeConstants.Scope.DEFAULT_ROLE;
    public static final String USER = NoticeConstants.Scope.DEFAULT_USER;

    private static final NoticeConstants DEFAULTS = new NoticeConstants();

    private NoticeScopeType() {
    }

    public static String all() {
        return constants().getScope().getAll();
    }

    public static String dept() {
        return constants().getScope().getDept();
    }

    public static String role() {
        return constants().getScope().getRole();
    }

    public static String user() {
        return constants().getScope().getUser();
    }

    public static boolean isSupported(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Strings.CI.equals(all(), normalized)
                || Strings.CI.equals(dept(), normalized)
                || Strings.CI.equals(role(), normalized)
                || Strings.CI.equals(user(), normalized);
    }

    private static NoticeConstants constants() {
        NoticeConstants bean = SpringContextHolder.getBean(NoticeConstants.class);
        return bean == null ? DEFAULTS : bean;
    }
}
