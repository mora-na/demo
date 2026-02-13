package com.example.demo.job.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
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

    private static final String APPENDER_NAME = "JOB_LOG_COLLECTOR";

    private final JobLogCollector collector;
    private final com.example.demo.job.config.JobLogCollectProperties properties;
    private JobLogAppender appender;

    @PostConstruct
    public void registerAppender() {
        if (!properties.isEnabled() || properties.getMaxLength() <= 0) {
            return;
        }
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        JobLogAppender jobAppender = new JobLogAppender(collector, properties);
        jobAppender.setName(APPENDER_NAME);
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
