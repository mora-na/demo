package com.example.demo.job.log;

import com.example.demo.job.config.JobConstants;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务执行日志收集器，按运行实例缓存日志内容。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Component
@RequiredArgsConstructor
public class JobLogCollector {

    private final JobConstants jobConstants;
    private final SysJobLogService jobLogService;
    private final Map<String, JobLogBuffer> buffers = new ConcurrentHashMap<>();
    private ScheduledExecutorService scheduler;

    public boolean isEnabled() {
        return jobConstants.getLogCollect().isEnabled();
    }

    public String getMdcKey() {
        return jobConstants.getLogCollect().getMdcKey();
    }

    public String getThreadKey() {
        return jobConstants.getLogCollect().getThreadKey();
    }

    public int getMaxLength() {
        return jobConstants.getLogCollect().getMaxLength();
    }

    public long getMergeDelayMillis() {
        return jobConstants.getLogCollect().getMergeDelayMillis();
    }

    public long getMaxHoldMillis() {
        return jobConstants.getLogCollect().getMaxHoldMillis();
    }

    public boolean isInheritThreadContext() {
        return jobConstants.getLogCollect().isInheritThreadContext();
    }

    public String start() {
        if (!jobConstants.getLogCollect().isEnabled() || jobConstants.getLogCollect().getMaxLength() <= 0) {
            return null;
        }
        String runId = UUID.randomUUID().toString();
        buffers.put(runId, new JobLogBuffer(jobConstants.getLogCollect().getMaxLength()));
        return runId;
    }

    public void append(String runId, String line) {
        if (!jobConstants.getLogCollect().isEnabled() || runId == null || line == null) {
            return;
        }
        JobLogBuffer buffer = buffers.get(runId);
        if (buffer == null) {
            return;
        }
        buffer.append(line, jobConstants.getLogCollect().getMaxHoldMillis());
    }

    public String finish(String runId) {
        if (runId == null) {
            return null;
        }
        JobLogBuffer buffer = buffers.get(runId);
        if (buffer == null) {
            return null;
        }
        buffer.markClosed();
        return buffer.getContent();
    }

    public void close(String runId) {
        if (runId == null) {
            return;
        }
        buffers.remove(runId);
    }

    public boolean shouldDelayMerge() {
        return jobConstants.getLogCollect().isEnabled()
                && jobConstants.getLogCollect().getMergeDelayMillis() > 0
                && jobConstants.getLogCollect().getMaxHoldMillis() > 0;
    }

    public void scheduleMerge(String runId, Long logId, String manualLog) {
        if (!shouldDelayMerge() || runId == null || logId == null) {
            close(runId);
            return;
        }
        long delay = jobConstants.getLogCollect().getMergeDelayMillis();
        scheduler.schedule(() -> mergeAndUpdate(runId, logId, manualLog), delay, TimeUnit.MILLISECONDS);
    }

    public String mergeLogs(String manual, String autoLog) {
        String left = manual == null ? "" : manual.trim();
        String right = autoLog == null ? "" : autoLog.trim();
        if (left.isEmpty()) {
            return right.isEmpty() ? null : trim(right);
        }
        if (right.isEmpty()) {
            return trim(left);
        }
        return trim(left + jobConstants.getExecution().getLogMergeSeparator() + right);
    }

    @PostConstruct
    public void startCleanup() {
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, jobConstants.getLogCollect().getCollectorThreadName());
            thread.setDaemon(true);
            return thread;
        });
        if (!jobConstants.getLogCollect().isEnabled()) {
            return;
        }
        scheduler.scheduleAtFixedRate(
                this::cleanupExpired,
                jobConstants.getLogCollect().getCleanupInitialDelayMillis(),
                jobConstants.getLogCollect().getCleanupIntervalMillis(),
                TimeUnit.MILLISECONDS);
    }

    @PreDestroy
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void mergeAndUpdate(String runId, Long logId, String manualLog) {
        try {
            JobLogBuffer buffer = buffers.remove(runId);
            String autoLog = buffer == null ? null : buffer.getContent();
            String merged = mergeLogs(manualLog, autoLog);
            if (merged == null) {
                return;
            }
            SysJobLog update = new SysJobLog();
            update.setId(logId);
            update.setLogDetail(merged);
            jobLogService.updateById(update);
        } finally {
            buffers.remove(runId);
        }
    }

    private void cleanupExpired() {
        if (buffers.isEmpty() || jobConstants.getLogCollect().getMaxHoldMillis() <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        long maxHoldMillis = jobConstants.getLogCollect().getMaxHoldMillis();
        for (Map.Entry<String, JobLogBuffer> entry : buffers.entrySet()) {
            JobLogBuffer buffer = entry.getValue();
            if (buffer == null) {
                continue;
            }
            if (buffer.isExpired(now, maxHoldMillis)) {
                buffers.remove(entry.getKey());
            }
        }
    }

    private String trim(String value) {
        if (value == null) {
            return null;
        }
        int maxLength = jobConstants.getLogCollect().getMaxLength();
        if (maxLength <= 0) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public boolean isActive(String runId) {
        return runId != null && buffers.containsKey(runId);
    }

    private static final class JobLogBuffer {
        private final int maxLength;
        private final StringBuilder builder = new StringBuilder();
        private boolean truncated = false;
        private volatile long closedAt = 0L;
        private volatile long lastAppendAt = 0L;

        private JobLogBuffer(int maxLength) {
            this.maxLength = Math.max(0, maxLength);
        }

        private synchronized void append(String line, long maxHoldMillis) {
            if (truncated || maxLength == 0) {
                return;
            }
            if (closedAt > 0 && maxHoldMillis > 0 && System.currentTimeMillis() - closedAt > maxHoldMillis) {
                return;
            }
            if (line.isEmpty()) {
                return;
            }
            int remaining = maxLength - builder.length();
            if (remaining <= 0) {
                truncated = true;
                return;
            }
            if (line.length() > remaining) {
                builder.append(line, 0, remaining);
                truncated = true;
                return;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line);
            lastAppendAt = System.currentTimeMillis();
        }

        private synchronized String getContent() {
            if (builder.length() == 0) {
                return null;
            }
            return builder.toString();
        }

        private void markClosed() {
            if (closedAt == 0L) {
                closedAt = System.currentTimeMillis();
            }
        }

        private boolean isExpired(long now, long maxHoldMillis) {
            if (closedAt == 0L || maxHoldMillis <= 0) {
                return false;
            }
            return now - closedAt > maxHoldMillis;
        }
    }
}
