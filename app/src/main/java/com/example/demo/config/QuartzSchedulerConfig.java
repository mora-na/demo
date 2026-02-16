package com.example.demo.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;

/**
 * Quartz 数据源绑定：从动态数据源中选择 job_rw，而不是额外注册一个 DataSource Bean。
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enabled", havingValue = "true", matchIfMissing = true)
public class QuartzSchedulerConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.datasource.dynamic.datasource.job_rw", name = "url")
    public SchedulerFactoryBeanCustomizer quartzSchedulerFactoryBeanCustomizer(ObjectProvider<DataSource> dataSourceProvider) {
        return schedulerFactoryBean -> {
            DataSource dataSource = dataSourceProvider.getIfAvailable();
            DynamicRoutingDataSource routingDataSource = unwrapDynamicRoutingDataSource(dataSource);
            if (routingDataSource == null) {
                throw new IllegalStateException("Cannot resolve DynamicRoutingDataSource from primary dataSource");
            }
            DataSource jobDataSource = routingDataSource.getDataSource("job_rw");
            if (jobDataSource == null) {
                throw new IllegalStateException("Missing dynamic datasource: job_rw");
            }
            schedulerFactoryBean.setDataSource(jobDataSource);
        };
    }

    private DynamicRoutingDataSource unwrapDynamicRoutingDataSource(DataSource dataSource) {
        if (dataSource == null) {
            return null;
        }
        if (dataSource instanceof DynamicRoutingDataSource) {
            return (DynamicRoutingDataSource) dataSource;
        }
        if (dataSource instanceof DelegatingDataSource) {
            return unwrapDynamicRoutingDataSource(((DelegatingDataSource) dataSource).getTargetDataSource());
        }
        return null;
    }
}
