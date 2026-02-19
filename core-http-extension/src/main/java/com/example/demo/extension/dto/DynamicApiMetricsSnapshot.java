package com.example.demo.extension.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 动态接口指标快照。
 */
@Data
@AllArgsConstructor
public class DynamicApiMetricsSnapshot {
    private DynamicApiMetricItem global;
    private List<DynamicApiMetricItem> items;
}
