package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.JobCreateRequest;
import com.example.demo.job.dto.JobQuery;
import com.example.demo.job.dto.JobUpdateRequest;
import com.example.demo.job.dto.JobVO;
import com.example.demo.job.entity.SysJob;
import com.example.demo.job.mapper.SysJobMapper;
import com.example.demo.job.service.JobSchedulerService;
import com.example.demo.job.service.SysJobService;
import com.example.demo.job.support.JobParamValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final JobParamValidator jobParamValidator;
    private final JobConstants jobConstants;

    @Override
    public List<SysJob> selectJobs(JobQuery query) {
        return list(buildQuery(query));
    }

    @Override
    public IPage<SysJob> selectJobsPage(Page<SysJob> page, JobQuery query) {
        Page<SysJob> resolved = page == null
                ? new Page<>(jobConstants.getPage().getDefaultPageNum(), jobConstants.getPage().getDefaultPageSize())
                : page;
        return this.page(resolved, buildQuery(query));
    }

    @Override
    @Transactional
    public SysJob createJob(JobCreateRequest request, AuthUser creator) {
        if (request == null) {
            return null;
        }
        if (!jobParamValidator.isValidCron(request.getCronExpression())) {
            return null;
        }
        if (!jobParamValidator.isValidHandler(request.getHandlerName())) {
            return null;
        }
        if (!jobParamValidator.isValidMisfirePolicy(request.getMisfirePolicy())) {
            return null;
        }
        Integer status = jobParamValidator.normalizeStatus(request.getStatus());
        if (status == null) {
            return null;
        }
        Integer allowConcurrent = jobParamValidator.normalizeConcurrent(request.getAllowConcurrent());
        if (allowConcurrent == null) {
            return null;
        }
        String misfirePolicy = jobParamValidator.normalizeMisfirePolicy(request.getMisfirePolicy());
        if (misfirePolicy == null) {
            return null;
        }
        SysJob job = new SysJob();
        job.setName(StringUtils.trimToEmpty(request.getName()));
        job.setHandlerName(StringUtils.trimToEmpty(request.getHandlerName()));
        job.setCronExpression(StringUtils.trimToEmpty(request.getCronExpression()));
        job.setStatus(status);
        job.setAllowConcurrent(allowConcurrent);
        job.setMisfirePolicy(misfirePolicy);
        job.setParams(request.getParams());
        job.setRemark(request.getRemark());
        if (creator != null) {
            job.setCreatedBy(creator.getId());
            job.setCreatedName(StringUtils.defaultIfBlank(creator.getNickName(), creator.getUserName()));
        }
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        if (!save(job)) {
            return null;
        }
        Long jobId = job.getId();
        runAfterCommit(() -> jobSchedulerService.syncJob(getById(jobId)));
        return job;
    }

    @Override
    @Transactional
    public boolean updateJob(Long id, JobUpdateRequest request) {
        if (id == null || request == null) {
            return false;
        }
        SysJob existing = getById(id);
        if (existing == null) {
            return false;
        }
        if (StringUtils.isNotBlank(request.getCronExpression())
                && !jobParamValidator.isValidCron(request.getCronExpression())) {
            return false;
        }
        if (StringUtils.isNotBlank(request.getHandlerName())
                && !jobParamValidator.isValidHandler(request.getHandlerName())) {
            return false;
        }
        if (StringUtils.isNotBlank(request.getMisfirePolicy())
                && !jobParamValidator.isValidMisfirePolicy(request.getMisfirePolicy())) {
            return false;
        }
        if (request.getStatus() != null && !jobParamValidator.isValidStatus(request.getStatus())) {
            return false;
        }
        if (request.getAllowConcurrent() != null && !jobParamValidator.isValidConcurrent(request.getAllowConcurrent())) {
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
            Integer status = jobParamValidator.normalizeStatus(request.getStatus());
            if (status == null) {
                return false;
            }
            job.setStatus(status);
        }
        if (request.getAllowConcurrent() != null) {
            Integer allowConcurrent = jobParamValidator.normalizeConcurrent(request.getAllowConcurrent());
            if (allowConcurrent == null) {
                return false;
            }
            job.setAllowConcurrent(allowConcurrent);
        }
        if (StringUtils.isNotBlank(request.getMisfirePolicy())) {
            String policy = jobParamValidator.normalizeMisfirePolicy(request.getMisfirePolicy());
            if (policy == null) {
                return false;
            }
            job.setMisfirePolicy(policy);
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
            runAfterCommit(() -> jobSchedulerService.syncJob(getById(id)));
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteJob(Long id) {
        if (id == null) {
            return false;
        }
        SysJob job = getById(id);
        if (job == null) {
            return false;
        }
        boolean removed = removeById(id);
        if (removed) {
            runAfterCommit(() -> jobSchedulerService.deleteJob(job));
        }
        return removed;
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, Integer status) {
        if (id == null || status == null) {
            return false;
        }
        if (!jobParamValidator.isValidStatus(status)) {
            return false;
        }
        SysJob job = new SysJob();
        job.setId(id);
        job.setStatus(jobParamValidator.normalizeStatus(status));
        job.setUpdatedAt(LocalDateTime.now());
        boolean updated = updateById(job);
        if (updated) {
            runAfterCommit(() -> jobSchedulerService.syncJob(getById(id)));
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

    private LambdaQueryWrapper<SysJob> buildQuery(JobQuery query) {
        if (query == null) {
            return Wrappers.lambdaQuery(SysJob.class)
                    .orderByDesc(SysJob::getId);
        }
        String name = StringUtils.trimToEmpty(query.getName());
        String handler = StringUtils.trimToEmpty(query.getHandlerName());
        return Wrappers.lambdaQuery(SysJob.class)
                .like(StringUtils.isNotBlank(name), SysJob::getName, name)
                .like(StringUtils.isNotBlank(handler), SysJob::getHandlerName, handler)
                .eq(query.getStatus() != null, SysJob::getStatus, query.getStatus())
                .orderByDesc(SysJob::getId);
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

    private void runAfterCommit(Runnable action) {
        if (action == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }

}
