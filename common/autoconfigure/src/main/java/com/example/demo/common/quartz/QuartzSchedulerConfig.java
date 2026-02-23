package com.example.demo.common.quartz;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

/**
 * Quartz 数据源绑定：从动态数据源中选择 job_rw，而不是额外注册一个 DataSource Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true", matchIfMissing = true)
public class QuartzSchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerConfig.class);

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.dynamic.datasource.job_rw", name = "url")
    public SchedulerFactoryBeanCustomizer quartzSchedulerFactoryBeanCustomizer(ObjectProvider<DataSource> dataSourceProvider) {
        return schedulerFactoryBean -> {
            DataSource dataSource = dataSourceProvider.getIfAvailable();
            if (dataSource == null) {
                log.warn("Quartz datasource binding skipped: primary DataSource not available");
                return;
            }
            DynamicRoutingDataSource routingDataSource = unwrapDynamicRoutingDataSource(dataSource);
            if (routingDataSource == null) {
                log.warn("Quartz datasource binding skipped: unable to resolve DynamicRoutingDataSource");
                return;
            }
            DataSource jobDataSource = routingDataSource.getDataSource("job_rw");
            if (jobDataSource == null) {
                log.warn("Quartz datasource binding skipped: dynamic datasource 'job_rw' not found");
                return;
            }
            schedulerFactoryBean.setDataSource(jobDataSource);
        };
    }

    private DynamicRoutingDataSource unwrapDynamicRoutingDataSource(DataSource dataSource) {
        Set<DataSource> visited = new HashSet<>();
        DataSource candidate = dataSource;
        while (candidate != null && visited.add(candidate)) {
            if (candidate instanceof DynamicRoutingDataSource) {
                return (DynamicRoutingDataSource) candidate;
            }
            if (candidate instanceof DelegatingDataSource) {
                candidate = ((DelegatingDataSource) candidate).getTargetDataSource();
                continue;
            }
            if (candidate instanceof Advised) {
                try {
                    Object target = ((Advised) candidate).getTargetSource().getTarget();
                    if (target instanceof DataSource) {
                        candidate = (DataSource) target;
                        continue;
                    }
                } catch (Exception ex) {
                    log.debug("Failed to unwrap advised DataSource", ex);
                }
            }
            break;
        }
        return null;
    }
}
