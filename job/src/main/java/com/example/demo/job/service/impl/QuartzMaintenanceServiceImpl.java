package com.example.demo.job.service.impl;

import com.example.demo.job.service.QuartzMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Quartz maintenance service implementation.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/17
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.quartz", name = "job-store-type", havingValue = "jdbc", matchIfMissing = true)
public class QuartzMaintenanceServiceImpl implements QuartzMaintenanceService {

    private static final String CRON_TRIGGER_TYPE = "CRON";

    private final DataSource dataSource;

    @Value("${spring.quartz.properties.org.quartz.jobStore.tablePrefix:sys_quartz_}")
    private String tablePrefix;

    @Value("${spring.quartz.properties.org.quartz.scheduler.instanceName:}")
    private String schedulerName;

    @Override
    public int cleanOrphanCronTriggers() {
        String prefix = normalizeTablePrefix(tablePrefix);
        if (prefix == null) {
            log.warn("Skip Quartz orphan trigger cleanup: invalid tablePrefix '{}'", tablePrefix);
            return 0;
        }
        List<TriggerKeyRow> orphans = loadOrphanCronTriggers(prefix);
        if (orphans.isEmpty()) {
            log.info("No orphan Quartz cron triggers found.");
            return 0;
        }
        int deleted = deleteTriggers(prefix, orphans);
        log.warn("Cleaned {} orphan Quartz cron triggers.", deleted);
        return deleted;
    }

    private List<TriggerKeyRow> loadOrphanCronTriggers(String prefix) {
        String triggersTable = prefix + "triggers";
        String cronTable = prefix + "cron_triggers";
        StringBuilder sql = new StringBuilder();
        sql.append("select t.sched_name, t.trigger_name, t.trigger_group ")
                .append("from ").append(triggersTable).append(" t ")
                .append("left join ").append(cronTable).append(" c ")
                .append("on t.sched_name = c.sched_name ")
                .append("and t.trigger_name = c.trigger_name ")
                .append("and t.trigger_group = c.trigger_group ")
                .append("where t.trigger_type = ? ")
                .append("and c.trigger_name is null ");
        boolean filterByScheduler = StringUtils.isNotBlank(schedulerName);
        if (filterByScheduler) {
            sql.append("and t.sched_name = ? ");
        }
        List<TriggerKeyRow> result = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setString(index++, CRON_TRIGGER_TYPE);
            if (filterByScheduler) {
                statement.setString(index, schedulerName);
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    TriggerKeyRow row = new TriggerKeyRow(
                            rs.getString("sched_name"),
                            rs.getString("trigger_name"),
                            rs.getString("trigger_group")
                    );
                    result.add(row);
                }
            }
        } catch (SQLException ex) {
            log.error("Failed to load orphan Quartz cron triggers.", ex);
        }
        return result;
    }

    private int deleteTriggers(String prefix, List<TriggerKeyRow> orphans) {
        String triggersTable = prefix + "triggers";
        String sql = "delete from " + triggersTable
                + " where sched_name = ? and trigger_name = ? and trigger_group = ?";
        int[] batch;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);
            for (TriggerKeyRow row : orphans) {
                statement.setString(1, row.getSchedName());
                statement.setString(2, row.getTriggerName());
                statement.setString(3, row.getTriggerGroup());
                statement.addBatch();
            }
            batch = statement.executeBatch();
            connection.commit();
        } catch (SQLException ex) {
            log.error("Failed to clean orphan Quartz triggers.", ex);
            return 0;
        }
        int deleted = 0;
        for (int count : batch) {
            if (count > 0) {
                deleted += count;
            }
        }
        return deleted;
    }

    private String normalizeTablePrefix(String raw) {
        String value = StringUtils.trimToEmpty(raw);
        if (value.isEmpty()) {
            return null;
        }
        if (!value.matches("[A-Za-z0-9_.]+")) {
            return null;
        }
        return value;
    }

    private static final class TriggerKeyRow {
        private final String schedName;
        private final String triggerName;
        private final String triggerGroup;

        private TriggerKeyRow(String schedName, String triggerName, String triggerGroup) {
            this.schedName = schedName;
            this.triggerName = triggerName;
            this.triggerGroup = triggerGroup;
        }

        private String getSchedName() {
            return schedName;
        }

        private String getTriggerName() {
            return triggerName;
        }

        private String getTriggerGroup() {
            return triggerGroup;
        }
    }
}
