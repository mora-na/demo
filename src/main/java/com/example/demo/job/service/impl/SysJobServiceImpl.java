package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.job.dto.JobCreateRequest;
import com.example.demo.job.dto.JobQuery;
import com.example.demo.job.dto.JobUpdateRequest;
import com.example.demo.job.dto.JobVO;
import com.example.demo.job.entity.SysJob;
import com.example.demo.job.mapper.SysJobMapper;
import com.example.demo.job.model.JobMisfirePolicy;
import com.example.demo.job.service.JobSchedulerService;
import com.example.demo.job.service.SysJobService;
import com.example.demo.job.support.JobHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 定时任务服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Service
@RequiredArgsConstructor
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    private final JobSchedulerService jobSchedulerService;
    private final JobHandlerRegistry jobHandlerRegistry;

    @Override
    public List<SysJob> selectJobs(JobQuery query) {
        if (query == null) {
            return list(Wrappers.lambdaQuery(SysJob.class)
                    .orderByDesc(SysJob::getId));
        }
        String name = StringUtils.trimToEmpty(query.getName());
        String handler = StringUtils.trimToEmpty(query.getHandlerName());
        return list(Wrappers.lambdaQuery(SysJob.class)
                .like(StringUtils.isNotBlank(name), SysJob::getName, name)
                .like(StringUtils.isNotBlank(handler), SysJob::getHandlerName, handler)
                .eq(query.getStatus() != null, SysJob::getStatus, query.getStatus())
                .orderByDesc(SysJob::getId));
    }

    @Override
    public SysJob createJob(JobCreateRequest request, AuthUser creator) {
        if (request == null) {
            return null;
        }
        if (!isValidCron(request.getCronExpression())) {
            return null;
        }
        if (!isHandlerValid(request.getHandlerName())) {
            return null;
        }
        SysJob job = new SysJob();
        job.setName(StringUtils.trimToEmpty(request.getName()));
        job.setHandlerName(StringUtils.trimToEmpty(request.getHandlerName()));
        job.setCronExpression(StringUtils.trimToEmpty(request.getCronExpression()));
        job.setStatus(normalizeStatus(request.getStatus()));
        job.setAllowConcurrent(normalizeConcurrent(request.getAllowConcurrent()));
        job.setMisfirePolicy(normalizeMisfirePolicy(request.getMisfirePolicy()));
        job.setParams(request.getParams());
        job.setRemark(request.getRemark());
        if (creator != null) {
            job.setCreatedBy(creator.getId());
            job.setCreatedName(StringUtils.defaultIfBlank(creator.getNickName(), creator.getUserName()));
        }
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        save(job);
        jobSchedulerService.syncJob(job);
        return job;
    }

    @Override
    public boolean updateJob(Long id, JobUpdateRequest request) {
        if (id == null || request == null) {
            return false;
        }
        SysJob existing = getById(id);
        if (existing == null) {
            return false;
        }
        if (StringUtils.isNotBlank(request.getCronExpression()) && !isValidCron(request.getCronExpression())) {
            return false;
        }
        if (StringUtils.isNotBlank(request.getHandlerName()) && !isHandlerValid(request.getHandlerName())) {
            return false;
        }
        SysJob job = new SysJob();
        job.setId(id);
        if (StringUtils.isNotBlank(request.getName())) {
            job.setName(request.getName());
        }
        if (StringUtils.isNotBlank(request.getHandlerName())) {
            job.setHandlerName(request.getHandlerName());
        }
        if (StringUtils.isNotBlank(request.getCronExpression())) {
            job.setCronExpression(request.getCronExpression());
        }
        if (request.getStatus() != null) {
            job.setStatus(normalizeStatus(request.getStatus()));
        }
        if (request.getAllowConcurrent() != null) {
            job.setAllowConcurrent(normalizeConcurrent(request.getAllowConcurrent()));
        }
        if (StringUtils.isNotBlank(request.getMisfirePolicy())) {
            job.setMisfirePolicy(normalizeMisfirePolicy(request.getMisfirePolicy()));
        }
        if (request.getParams() != null) {
            job.setParams(request.getParams());
        }
        if (request.getRemark() != null) {
            job.setRemark(request.getRemark());
        }
        job.setUpdatedAt(LocalDateTime.now());
        boolean updated = updateById(job);
        if (updated) {
            SysJob latest = getById(id);
            jobSchedulerService.syncJob(latest);
        }
        return updated;
    }

    @Override
    public boolean deleteJob(Long id) {
        if (id == null) {
            return false;
        }
        SysJob job = getById(id);
        if (job == null) {
            return false;
        }
        jobSchedulerService.deleteJob(job);
        return removeById(id);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        SysJob job = new SysJob();
        job.setId(id);
        job.setStatus(normalizeStatus(status));
        job.setUpdatedAt(LocalDateTime.now());
        boolean updated = updateById(job);
        if (updated) {
            SysJob latest = getById(id);
            jobSchedulerService.syncJob(latest);
        }
        return updated;
    }

    @Override
    public boolean runOnce(Long id) {
        if (id == null) {
            return false;
        }
        SysJob job = getById(id);
        if (job == null) {
            return false;
        }
        jobSchedulerService.syncJob(job);
        jobSchedulerService.runOnce(job);
        return true;
    }

    @Override
    public JobVO toView(SysJob job) {
        if (job == null) {
            return null;
        }
        JobVO view = new JobVO();
        view.setId(job.getId());
        view.setName(job.getName());
        view.setHandlerName(job.getHandlerName());
        view.setCronExpression(job.getCronExpression());
        view.setStatus(job.getStatus());
        view.setAllowConcurrent(job.getAllowConcurrent());
        view.setMisfirePolicy(job.getMisfirePolicy());
        view.setParams(job.getParams());
        view.setRemark(job.getRemark());
        view.setCreatedName(job.getCreatedName());
        view.setCreatedAt(job.getCreatedAt());
        view.setUpdatedAt(job.getUpdatedAt());
        view.setNextFireTime(jobSchedulerService.getNextFireTime(job));
        return view;
    }

    @Override
    public List<JobVO> toViewList(List<SysJob> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return Collections.emptyList();
        }
        List<JobVO> views = new ArrayList<>();
        for (SysJob job : jobs) {
            JobVO view = toView(job);
            if (view != null) {
                views.add(view);
            }
        }
        return views;
    }

    private boolean isHandlerValid(String handlerName) {
        if (StringUtils.isBlank(handlerName)) {
            return false;
        }
        return jobHandlerRegistry.getHandler(handlerName) != null;
    }

    private boolean isValidCron(String cronExpression) {
        if (StringUtils.isBlank(cronExpression)) {
            return false;
        }
        return CronExpression.isValidExpression(cronExpression.trim());
    }

    private int normalizeStatus(Integer status) {
        if (status == null) {
            return SysJob.STATUS_ENABLED;
        }
        if (status == SysJob.STATUS_DISABLED) {
            return SysJob.STATUS_DISABLED;
        }
        return SysJob.STATUS_ENABLED;
    }

    private int normalizeConcurrent(Integer allowConcurrent) {
        if (allowConcurrent == null) {
            return 1;
        }
        return allowConcurrent == 0 ? 0 : 1;
    }

    private String normalizeMisfirePolicy(String policy) {
        if (StringUtils.isBlank(policy)) {
            return JobMisfirePolicy.DEFAULT;
        }
        String normalized = policy.trim().toUpperCase(Locale.ROOT);
        if (!JobMisfirePolicy.isSupported(normalized)) {
            return JobMisfirePolicy.DEFAULT;
        }
        return normalized;
    }

}
