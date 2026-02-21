package com.example.demo.job.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Starts Quartz scheduler after maintenance and job bootstrap.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/22
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.quartz", name = "job-store-type", havingValue = "jdbc", matchIfMissing = true)
public class QuartzSchedulerStarter {

    private final Scheduler scheduler;

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.LOWEST_PRECEDENCE)
    public void startScheduler() {
        try {
            if (scheduler.isShutdown()) {
                log.warn("Quartz scheduler is shutdown, skip start.");
                return;
            }
            if (!scheduler.isStarted()) {
                scheduler.start();
                log.info("Quartz scheduler started.");
            }
        } catch (SchedulerException ex) {
            log.error("Failed to start Quartz scheduler.", ex);
        }
    }
}
