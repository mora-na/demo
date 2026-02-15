package com.example.demo.dict.web;

import com.example.demo.dict.support.DictLabelContextHolder;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求结束后清理字典标签上下文，防止线程复用导致数据串扰。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/15
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DictLabelContextCleanupFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            DictLabelContextHolder.clear();
        }
    }
}
