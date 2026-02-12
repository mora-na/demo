package com.example.demo.job.support;

import com.example.demo.job.dto.JobHandlerInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 任务处理器注册表。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Component
public class JobHandlerRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public JobHandler getHandler(String name) {
        if (applicationContext == null || StringUtils.isBlank(name)) {
            return null;
        }
        Map<String, JobHandler> handlers = applicationContext.getBeansOfType(JobHandler.class);
        if (handlers.containsKey(name)) {
            return handlers.get(name);
        }
        String normalized = name.trim();
        for (Map.Entry<String, JobHandler> entry : handlers.entrySet()) {
            JobHandler handler = entry.getValue();
            if (handler == null) {
                continue;
            }
            String simpleName = handler.getClass().getSimpleName();
            if (simpleName.equalsIgnoreCase(normalized)) {
                return handler;
            }
        }
        return null;
    }

    public List<JobHandlerInfo> listHandlers() {
        if (applicationContext == null) {
            return new ArrayList<>();
        }
        Map<String, JobHandler> handlers = applicationContext.getBeansOfType(JobHandler.class);
        List<JobHandlerInfo> result = new ArrayList<>();
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
}
