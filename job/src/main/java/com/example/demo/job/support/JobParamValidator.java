package com.example.demo.job.support;

import com.example.demo.job.config.JobConstants;
import com.example.demo.job.model.JobMisfirePolicy;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Component;

import java.util.Locale;


/**
 * Job parameter validator and normalizer.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/20
 */
@Component
@RequiredArgsConstructor
public class JobParamValidator {

    private final JobHandlerRegistry jobHandlerRegistry;
    private final JobConstants jobConstants;

    public boolean isValidCron(String cronExpression) {
        if (StringUtils.isBlank(cronExpression)) {
            return false;
        }
        return CronExpression.isValidExpression(cronExpression.trim());
    }

    public boolean isValidHandler(String handlerName) {
        if (StringUtils.isBlank(handlerName)) {
            return false;
        }
        return jobHandlerRegistry.getHandler(handlerName) != null;
    }

    public boolean isValidMisfirePolicy(String policy) {
        return StringUtils.isBlank(policy) || JobMisfirePolicy.isSupported(policy);
    }

    public boolean isValidStatus(Integer status) {
        if (status == null) {
            return true;
        }
        return status == jobConstants.getStatus().getJobEnabled()
                || status == jobConstants.getStatus().getJobDisabled();
    }

    public boolean isValidConcurrent(Integer allowConcurrent) {
        if (allowConcurrent == null) {
            return true;
        }
        return allowConcurrent == jobConstants.getConcurrent().getAllow()
                || allowConcurrent == jobConstants.getConcurrent().getDisallow();
    }

    public boolean isEnabled(Integer status) {
        return status != null && status == jobConstants.getStatus().getJobEnabled();
    }

    public Integer normalizeStatus(Integer status) {
        if (status == null) {
            return jobConstants.getStatus().getJobEnabled();
        }
        if (isValidStatus(status)) {
            return status;
        }
        return null;
    }

    public Integer normalizeConcurrent(Integer allowConcurrent) {
        if (allowConcurrent == null) {
            return jobConstants.getConcurrent().getAllow();
        }
        if (isValidConcurrent(allowConcurrent)) {
            return allowConcurrent;
        }
        return null;
    }

    public String normalizeMisfirePolicy(String policy) {
        if (StringUtils.isBlank(policy)) {
            return JobMisfirePolicy.DEFAULT;
        }
        String normalized = policy.trim().toUpperCase(Locale.ROOT);
        return JobMisfirePolicy.isSupported(normalized) ? normalized : null;
    }
}
