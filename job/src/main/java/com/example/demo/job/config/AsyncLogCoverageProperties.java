package com.example.demo.job.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;

/**
 * Configuration for async log coverage runs.
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "job.async-log.coverage")
public class AsyncLogCoverageProperties {

    /**
     * baseline | branches
     */
    private String mode = AsyncLogCoverageMode.BASELINE.getValue();

    /**
     * Timeout used by the forced timeout probe in branches mode.
     */
    @Min(1)
    private long branchWaitTimeoutSeconds = 1L;

    public AsyncLogCoverageMode getModeEnum() {
        return AsyncLogCoverageMode.from(mode);
    }
}
