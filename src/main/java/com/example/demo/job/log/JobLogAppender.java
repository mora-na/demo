package com.example.demo.job.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.AppenderBase;
import com.example.demo.job.config.JobConstants;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Job 日志收集 Appender，仅在命中 MDC/线程范围时收集。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
public class JobLogAppender extends AppenderBase<ILoggingEvent> {

    private final JobLogCollector collector;
    private final JobConstants jobConstants;

    public JobLogAppender(JobLogCollector collector, JobConstants jobConstants) {
        this.collector = collector;
        this.jobConstants = jobConstants;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event == null || !jobConstants.getLogCollect().isEnabled() || collector.getMaxLength() <= 0) {
            return;
        }
        Level minLevel = Level.toLevel(jobConstants.getLogCollect().getMinLevel(), Level.INFO);
        if (event.getLevel() == null || !event.getLevel().isGreaterOrEqual(minLevel)) {
            return;
        }
        String runId = resolveRunId(event);
        if (runId == null) {
            return;
        }
        String line = formatLine(event);
        if (!line.isEmpty()) {
            collector.append(runId, line);
        }
    }

    private String resolveRunId(ILoggingEvent event) {
        JobConstants.LogCollect.Scope scope = jobConstants.getLogCollect().getScope();
        Map<String, String> mdc = event.getMDCPropertyMap();
        if (mdc == null || mdc.isEmpty()) {
            return resolveThreadContextRunId();
        }
        String runId = mdc.get(jobConstants.getLogCollect().getMdcKey());
        if (runId == null || runId.isEmpty()) {
            return resolveThreadContextRunId();
        }
        if (scope == JobConstants.LogCollect.Scope.THREAD) {
            String threadKey = jobConstants.getLogCollect().getThreadKey();
            String expected = mdc.get(threadKey);
            if (expected == null || !expected.equals(event.getThreadName())) {
                return null;
            }
        }
        return collector.isActive(runId) ? runId : null;
    }

    private String formatLine(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        if (message == null) {
            message = jobConstants.getAppender().getEmptyMessage();
        }
        String time = Instant.ofEpochMilli(event.getTimeStamp())
                .atZone(ZoneId.systemDefault())
                .format(resolveTimeFormatter());
        String logger = event.getLoggerName();
        String base = time + " " + event.getLevel() + " " + logger + " - " + message;
        IThrowableProxy throwable = event.getThrowableProxy();
        if (throwable == null) {
            return base;
        }
        return base + jobConstants.getAppender().getThrowableSeparator() + ThrowableProxyUtil.asString(throwable);
    }

    private String resolveThreadContextRunId() {
        if (!collector.isInheritThreadContext()) {
            return null;
        }
        String runId = JobLogThreadContext.get();
        return collector.isActive(runId) ? runId : null;
    }

    private DateTimeFormatter resolveTimeFormatter() {
        return DateTimeFormatter.ofPattern(jobConstants.getAppender().getTimePattern());
    }
}
