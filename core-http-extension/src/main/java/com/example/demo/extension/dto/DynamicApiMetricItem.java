package com.example.demo.extension.dto;

import lombok.Data;

/**
 * 动态接口指标明细。
 */
@Data
public class DynamicApiMetricItem {
    private String name;
    private Long apiId;
    private String path;
    private String method;
    private String type;
    private long total;
    private long success;
    private long failure;
    private long timeout;
    private long cancelled;
    private long rejected;
    private long circuitOpen;
    private long inflight;
    private double avgQueueMs;
    private double avgExecuteMs;
    private double avgTotalMs;
    private long lastUpdateTime;
}
