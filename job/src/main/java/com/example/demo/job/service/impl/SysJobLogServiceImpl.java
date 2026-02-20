package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.JobLogDetailVO;
import com.example.demo.job.dto.JobLogQuery;
import com.example.demo.job.dto.JobLogVO;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.mapper.SysJobLogMapper;
import com.example.demo.job.service.SysJobLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 定时任务日志服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Service
@RequiredArgsConstructor
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    private final JobConstants jobConstants;

    @Override
    public List<SysJobLog> selectLogs(JobLogQuery query) {
        return list(buildQuery(query));
    }

    @Override
    public IPage<SysJobLog> selectLogsPage(Page<SysJobLog> page, JobLogQuery query) {
        Page<SysJobLog> resolved = page == null
                ? new Page<>(jobConstants.getPage().getDefaultPageNum(), jobConstants.getPage().getDefaultPageSize())
                : page;
        return this.page(resolved, buildQuery(query));
    }

    @Override
    public List<JobLogVO> toViewList(List<SysJobLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return Collections.emptyList();
        }
        List<JobLogVO> result = new ArrayList<>();
        for (SysJobLog log : logs) {
            if (log == null) {
                continue;
            }
            JobLogVO view = new JobLogVO();
            view.setId(log.getId());
            view.setJobId(log.getJobId());
            view.setJobName(log.getJobName());
            view.setHandlerName(log.getHandlerName());
            view.setStatus(log.getStatus());
            view.setMessage(log.getMessage());
            view.setStartTime(log.getStartTime());
            view.setEndTime(log.getEndTime());
            view.setDurationMs(log.getDurationMs());
            result.add(view);
        }
        return result;
    }

    @Override
    public JobLogDetailVO toDetailView(SysJobLog log) {
        if (log == null) {
            return null;
        }
        JobLogDetailVO view = new JobLogDetailVO();
        view.setId(log.getId());
        view.setJobId(log.getJobId());
        view.setJobName(log.getJobName());
        view.setHandlerName(log.getHandlerName());
        view.setStatus(log.getStatus());
        view.setMessage(log.getMessage());
        view.setLogDetail(log.getLogDetail());
        view.setStartTime(log.getStartTime());
        view.setEndTime(log.getEndTime());
        view.setDurationMs(log.getDurationMs());
        return view;
    }

    private LambdaQueryWrapper<SysJobLog> buildQuery(JobLogQuery query) {
        LambdaQueryWrapper<SysJobLog> wrapper =
                Wrappers.lambdaQuery(SysJobLog.class)
                        .select(SysJobLog::getId,
                                SysJobLog::getJobId,
                                SysJobLog::getJobName,
                                SysJobLog::getHandlerName,
                                SysJobLog::getStatus,
                                SysJobLog::getMessage,
                                SysJobLog::getStartTime,
                                SysJobLog::getEndTime,
                                SysJobLog::getDurationMs);
        if (query == null) {
            return wrapper
                    .orderByDesc(SysJobLog::getStartTime)
                    .orderByDesc(SysJobLog::getId);
        }
        return wrapper
                .eq(query.getJobId() != null, SysJobLog::getJobId, query.getJobId())
                .orderByDesc(SysJobLog::getStartTime)
                .orderByDesc(SysJobLog::getId);
    }
}
