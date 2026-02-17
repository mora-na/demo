package com.example.demo.job.support;

import com.example.demo.job.service.QuartzMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Cleans up Quartz persistence anomalies after startup.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/17
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.quartz", name = "job-store-type", havingValue = "jdbc", matchIfMissing = true)
public class QuartzMaintenanceBootstrap {

    private final QuartzMaintenanceService maintenanceService;

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent.class)
    public void cleanOrphans() {
        int cleaned = maintenanceService.cleanOrphanCronTriggers();
        if (cleaned > 0) {
            log.warn("Quartz orphan cleanup removed {} trigger(s).", cleaned);
        }
    }
}
