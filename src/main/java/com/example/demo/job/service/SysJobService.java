package com.example.demo.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.job.dto.JobCreateRequest;
import com.example.demo.job.dto.JobQuery;
import com.example.demo.job.dto.JobUpdateRequest;
import com.example.demo.job.dto.JobVO;
import com.example.demo.job.entity.SysJob;

import java.util.List;

/**
 * 定时任务服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface SysJobService extends IService<SysJob> {

    List<SysJob> selectJobs(JobQuery query);

    IPage<SysJob> selectJobsPage(Page<SysJob> page, JobQuery query);

    SysJob createJob(JobCreateRequest request, AuthUser creator);

    boolean updateJob(Long id, JobUpdateRequest request);

    boolean deleteJob(Long id);

    boolean updateStatus(Long id, Integer status);

    boolean runOnce(Long id);

    JobVO toView(SysJob job);

    List<JobVO> toViewList(List<SysJob> jobs);
}
