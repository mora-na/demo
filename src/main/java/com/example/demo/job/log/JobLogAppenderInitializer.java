package com.example.demo.job.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.example.demo.job.config.JobConstants;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 注册 Job 日志收集 Appender。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Component
@RequiredArgsConstructor
public class JobLogAppenderInitializer {

    private final JobLogCollector collector;
    private final JobConstants jobConstants;
    private JobLogAppender appender;

    @PostConstruct
    public void registerAppender() {
        if (!jobConstants.getLogCollect().isEnabled() || jobConstants.getLogCollect().getMaxLength() <= 0) {
            return;
        }
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        JobLogAppender jobAppender = new JobLogAppender(collector, jobConstants);
        jobAppender.setName(jobConstants.getAppender().getAppenderName());
        jobAppender.setContext(context);
        jobAppender.start();
        root.addAppender(jobAppender);
        this.appender = jobAppender;
    }

    @PreDestroy
    public void removeAppender() {
        if (appender == null) {
            return;
        }
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAppender(appender);
        appender.stop();
        appender = null;
    }
}
