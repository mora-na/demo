package com.example.demo.job.log;

import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.JobLogCollectorMetricsVO;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.service.SysJobLogService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 定时任务执行日志收集器，按运行实例缓存日志内容。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JobLogCollector {

    private final JobConstants jobConstants;
    private final SysJobLogService jobLogService;
    private final AtomicLong lastDegradeLogAt = new AtomicLong(0L);
    private ScheduledExecutorService scheduler;
    private Cache<String, JobLogBuffer> buffers;

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
        if (buffers == null) {
            return null;
        }
        if (isDegraded()) {
            logDegradeOnce();
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
        if (buffers == null) {
            return;
        }
        if (isDegraded()) {
            return;
        }
        JobLogBuffer buffer = buffers.getIfPresent(runId);
        if (buffer == null) {
            return;
        }
        buffer.append(line, jobConstants.getLogCollect().getMaxHoldMillis());
    }

    public String finish(String runId) {
        if (runId == null) {
            return null;
        }
        if (buffers == null) {
            return null;
        }
        JobLogBuffer buffer = buffers.getIfPresent(runId);
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
        if (buffers == null) {
            return;
        }
        buffers.invalidate(runId);
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
        if (buffers == null) {
            return;
        }
        JobLogBuffer buffer = buffers.getIfPresent(runId);
        if (buffer != null) {
            buffer.attachLog(logId, manualLog);
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
        this.buffers = buildBufferCache();
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
            JobLogBuffer buffer = buffers.getIfPresent(runId);
            if (buffer == null) {
                return;
            }
            String autoLog = buffer.getContent();
            String storedManual = buffer.getManualLog();
            String merged = mergeLogs(storedManual == null ? manualLog : storedManual, autoLog);
            if (merged == null) {
                return;
            }
            SysJobLog update = new SysJobLog();
            update.setId(logId);
            update.setLogDetail(merged);
            jobLogService.updateById(update);
        } finally {
            buffers.invalidate(runId);
        }
    }

    private void cleanupExpired() {
        if (buffers == null || buffers.estimatedSize() == 0) {
            return;
        }
        buffers.cleanUp();
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
        return runId != null && buffers != null && buffers.getIfPresent(runId) != null;
    }

    public JobLogCollectorMetricsVO snapshotMetrics() {
        JobLogCollectorMetricsVO metrics = new JobLogCollectorMetricsVO();
        JobConstants.LogCollect config = jobConstants.getLogCollect();
        metrics.setEnabled(config.isEnabled());
        metrics.setAutoDegradeEnabled(config.isAutoDegradeEnabled());
        metrics.setDegraded(isDegraded());
        metrics.setBufferSize(buffers == null ? 0 : buffers.estimatedSize());
        metrics.setMaxBuffers(config.getMaxBuffers());
        metrics.setMaxLength(config.getMaxLength());
        metrics.setMaxHoldMillis(config.getMaxHoldMillis());
        metrics.setMergeDelayMillis(config.getMergeDelayMillis());
        metrics.setDegradeBufferRatio(resolveDegradeRatio());
        return metrics;
    }

    private Cache<String, JobLogBuffer> buildBufferCache() {
        JobConstants.LogCollect config = jobConstants.getLogCollect();
        int maxBuffers = Math.max(1, config.getMaxBuffers());
        long maxHoldMillis = Math.max(1000L, config.getMaxHoldMillis());
        return Caffeine.newBuilder()
                .maximumSize(maxBuffers)
                .expireAfterAccess(Duration.ofMillis(maxHoldMillis))
                .removalListener((String runId, JobLogBuffer buffer, RemovalCause cause) -> {
                    if (buffer == null || cause == RemovalCause.EXPLICIT) {
                        return;
                    }
                    flushBuffer(buffer);
                })
                .build();
    }

    private boolean isDegraded() {
        JobConstants.LogCollect config = jobConstants.getLogCollect();
        if (!config.isAutoDegradeEnabled() || buffers == null) {
            return false;
        }
        long size = buffers.estimatedSize();
        int max = Math.max(1, config.getMaxBuffers());
        double ratio = resolveDegradeRatio();
        return ratio > 0 && size >= Math.ceil(max * ratio);
    }

    private double resolveDegradeRatio() {
        double ratio = jobConstants.getLogCollect().getDegradeBufferRatio();
        return ratio <= 0 ? 0d : Math.min(1d, ratio);
    }

    private void logDegradeOnce() {
        long now = System.currentTimeMillis();
        long last = lastDegradeLogAt.get();
        if (now - last < 60000L) {
            return;
        }
        if (lastDegradeLogAt.compareAndSet(last, now)) {
            log.warn("Job log collector degraded: buffer size={}, max={}, ratio={}",
                    buffers == null ? 0 : buffers.estimatedSize(),
                    jobConstants.getLogCollect().getMaxBuffers(),
                    resolveDegradeRatio());
        }
    }

    private void flushBuffer(JobLogBuffer buffer) {
        if (buffer == null || buffer.getLogId() == null) {
            return;
        }
        String autoLog = buffer.getContent();
        if (autoLog == null) {
            return;
        }
        String merged = mergeLogs(buffer.getManualLog(), autoLog);
        if (merged == null) {
            return;
        }
        try {
            SysJobLog update = new SysJobLog();
            update.setId(buffer.getLogId());
            update.setLogDetail(merged);
            jobLogService.updateById(update);
        } catch (Exception ex) {
            log.warn("Job log flush failed: logId={}, error={}", buffer.getLogId(), ex.getMessage());
        }
    }

    private static final class JobLogBuffer {
        private final int maxLength;
        private final StringBuilder builder = new StringBuilder();
        private boolean truncated = false;
        private volatile long closedAt = 0L;
        private volatile Long logId;
        private volatile String manualLog;

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
        }

        private synchronized String getContent() {
            if (builder.length() == 0) {
                return null;
            }
            return builder.toString();
        }

        private void attachLog(Long logId, String manualLog) {
            if (logId != null) {
                this.logId = logId;
            }
            if (manualLog != null) {
                this.manualLog = manualLog;
            }
        }

        private Long getLogId() {
            return logId;
        }

        private String getManualLog() {
            return manualLog;
        }

        private void markClosed() {
            if (closedAt == 0L) {
                closedAt = System.currentTimeMillis();
            }
        }

    }
}
