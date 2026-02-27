package com.example.demo.common.cluster;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cluster mode switch and override map.
 *
 * <p>Used by environment post-processor to force multi-node-safe configuration
 * when {@code server.is-clustered=true}. Exposed here to make IDEs recognize
 * the custom server.* properties.</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "server")
public class ServerClusteredProperties {

    /**
     * Whether the application runs in clustered (multi-node) mode.
     */
    private Boolean isClustered = false;

    /**
     * Key/value overrides applied when {@link #isClustered} is true.
     * Uses dotted property keys and supports any value type.
     */
    private Map<String, Object> clusteredCoverConfig = new LinkedHashMap<>();
}
