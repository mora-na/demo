package com.example.demo.job.log;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.example.demo.job.config.JobConstants;

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
    private PatternLayout layout;

    public JobLogAppender(JobLogCollector collector, JobConstants jobConstants) {
        this.collector = collector;
        this.jobConstants = jobConstants;
    }

    @Override
    public void start() {
        PatternLayout patternLayout = new PatternLayout();
        patternLayout.setContext(getContext());
        String pattern = jobConstants.getAppender().getPattern();
        patternLayout.setPattern(pattern == null || pattern.trim().isEmpty()
                ? JobConstants.Appender.DEFAULT_PATTERN
                : pattern);
        patternLayout.start();
        this.layout = patternLayout;
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (event == null || !jobConstants.getLogCollect().isEnabled() || collector.getMaxLength() <= 0) {
            return;
        }
        String runId = resolveRunId(event);
        if (runId == null) {
            return;
        }
        if (event.getLevel() == null || !collector.shouldCollect(runId, event.getLevel())) {
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
        if (layout == null) {
            return "";
        }
        String raw = layout.doLayout(event);
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        if (raw.endsWith("\r\n")) {
            return raw.substring(0, raw.length() - 2);
        }
        if (raw.endsWith("\n")) {
            return raw.substring(0, raw.length() - 1);
        }
        return raw;
    }

    private String resolveThreadContextRunId() {
        if (!collector.isInheritThreadContext()) {
            return null;
        }
        String runId = JobLogThreadContext.get();
        return collector.isActive(runId) ? runId : null;
    }

}
