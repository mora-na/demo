package com.example.demo.job.dto;

import lombok.Data;

/**
 * JobLogCollector 指标快照。
 */
@Data
public class JobLogCollectorMetricsVO {
    private boolean enabled;
    private boolean autoDegradeEnabled;
    private boolean degraded;
    private long bufferSize;
    private int maxBuffers;
    private int maxLength;
    private long maxHoldMillis;
    private long mergeDelayMillis;
    private double degradeBufferRatio;
}
