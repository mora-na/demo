package com.example.demo.config.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 跨节点配置变更信号（不包含敏感值）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeSignal implements Serializable {

    private static final long serialVersionUID = 1L;

    private String group;
    private String key;
    private Integer version;
    private boolean hotUpdate;
    private String nodeId;
    private long timestamp;
}
