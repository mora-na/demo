package com.example.demo.job.service;

/**
 * Quartz maintenance service (fixes persistence inconsistencies on startup).
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/17
 */
public interface QuartzMaintenanceService {

    /**
     * Removes cron triggers that miss their subtype row.
     *
     * @return number of deleted triggers
     */
    int cleanOrphanCronTriggers();
}
