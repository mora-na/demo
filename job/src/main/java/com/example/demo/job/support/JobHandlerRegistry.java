package com.example.demo.job.support;

import com.example.demo.job.api.JobHandler;
import com.example.demo.job.dto.JobHandlerInfo;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 任务处理器注册表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Component
public class JobHandlerRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private volatile Map<String, JobHandler> handlerCache;
    private volatile Map<String, JobHandler> simpleNameCache;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.handlerCache = null;
        this.simpleNameCache = null;
    }

    public JobHandler getHandler(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        ensureCache();
        Map<String, JobHandler> handlers = handlerCache;
        if (handlers == null || handlers.isEmpty()) {
            return null;
        }
        JobHandler direct = handlers.get(name);
        if (direct != null) {
            return direct;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        Map<String, JobHandler> bySimpleName = simpleNameCache;
        return bySimpleName == null ? null : bySimpleName.get(normalized);
    }

    public List<JobHandlerInfo> listHandlers() {
        ensureCache();
        Map<String, JobHandler> handlers = handlerCache;
        if (handlers == null || handlers.isEmpty()) {
            return Collections.emptyList();
        }
        List<JobHandlerInfo> result = new ArrayList<>(handlers.size());
        for (Map.Entry<String, JobHandler> entry : handlers.entrySet()) {
            JobHandler handler = entry.getValue();
            if (handler == null) {
                continue;
            }
            JobHandlerInfo info = new JobHandlerInfo();
            info.setName(entry.getKey());
            info.setClassName(handler.getClass().getName());
            result.add(info);
        }
        return result;
    }

    private void ensureCache() {
        if (handlerCache != null && simpleNameCache != null) {
            return;
        }
        synchronized (this) {
            if (handlerCache != null && simpleNameCache != null) {
                return;
            }
            if (applicationContext == null) {
                handlerCache = Collections.emptyMap();
                simpleNameCache = Collections.emptyMap();
                return;
            }
            Map<String, JobHandler> handlers = applicationContext.getBeansOfType(JobHandler.class);
            Map<String, JobHandler> simpleNames = new HashMap<>();
            for (Map.Entry<String, JobHandler> entry : handlers.entrySet()) {
                JobHandler handler = entry.getValue();
                if (handler == null) {
                    continue;
                }
                String key = handler.getClass().getSimpleName().toLowerCase(Locale.ROOT);
                simpleNames.putIfAbsent(key, handler);
            }
            handlerCache = handlers;
            simpleNameCache = simpleNames;
        }
    }
}
