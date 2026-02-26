package com.example.demo.common.cluster;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Provide a stable node id for the current process.
 */
@Component
public class NodeIdProvider {

    private final String nodeId;

    public NodeIdProvider(Environment environment) {
        String configured = environment == null ? null : environment.getProperty("app.node-id");
        if (configured != null && !configured.trim().isEmpty()) {
            this.nodeId = configured.trim();
            return;
        }
        this.nodeId = buildDefaultNodeId();
    }

    public String get() {
        return nodeId;
    }

    private String buildDefaultNodeId() {
        String host = "unknown";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (Exception ignored) {
        }
        String pid = "0";
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int at = name.indexOf('@');
            pid = at > 0 ? name.substring(0, at) : name;
        } catch (Exception ignored) {
        }
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return host + "-" + pid + "-" + suffix;
    }
}
