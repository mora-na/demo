package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.dto.JobLogDetailQuery;
import com.example.demo.job.dto.JobLogDetailVO;
import com.example.demo.job.entity.SysJobLogDetail;
import com.example.demo.job.mapper.SysJobLogDetailMapper;
import com.example.demo.job.model.JobLogDetailLevel;
import com.example.demo.job.service.SysJobLogDetailService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 定时任务执行明细日志服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Service
@RequiredArgsConstructor
public class SysJobLogDetailServiceImpl extends ServiceImpl<SysJobLogDetailMapper, SysJobLogDetail>
        implements SysJobLogDetailService {

    private final JobConstants jobConstants;

    @Override
    public IPage<SysJobLogDetail> selectJobLogDetailsPage(Page<SysJobLogDetail> page, JobLogDetailQuery query) {
        Page<SysJobLogDetail> resolved = page == null
                ? new Page<>(jobConstants.getPage().getDefaultPageNum(), jobConstants.getPage().getDefaultPageSize())
                : page;
        return this.page(resolved, buildQuery(query));
    }

    @Override
    public SysJobLogDetail appendDetail(Long jobLogId,
                                        String logLevel,
                                        LocalDateTime logStartTime,
                                        LocalDateTime logEndTime,
                                        String logContent) {
        if (jobLogId == null) {
            return null;
        }
        String content = StringUtils.trimToNull(logContent);
        if (content == null) {
            return null;
        }
        LocalDateTime start = logStartTime == null ? LocalDateTime.now() : logStartTime;
        LocalDateTime end = logEndTime == null ? start : logEndTime;
        if (end.isBefore(start)) {
            LocalDateTime swap = start;
            start = end;
            end = swap;
        }
        SysJobLogDetail detail = new SysJobLogDetail();
        detail.setJobLogId(jobLogId);
        detail.setLogLevel(normalizeLevel(logLevel));
        detail.setLogStartTime(start);
        detail.setLogEndTime(end);
        detail.setLogContent(content);
        if (!save(detail)) {
            return null;
        }
        return detail;
    }

    @Override
    public JobLogDetailVO toView(SysJobLogDetail detail) {
        if (detail == null) {
            return null;
        }
        JobLogDetailVO view = new JobLogDetailVO();
        view.setId(detail.getId());
        view.setJobLogId(detail.getJobLogId());
        view.setLogLevel(detail.getLogLevel());
        view.setLogStartTime(detail.getLogStartTime());
        view.setLogEndTime(detail.getLogEndTime());
        view.setLogContent(detail.getLogContent());
        return view;
    }

    private LambdaQueryWrapper<SysJobLogDetail> buildQuery(JobLogDetailQuery query) {
        if (query == null) {
            return Wrappers.lambdaQuery(SysJobLogDetail.class)
                    .orderByAsc(SysJobLogDetail::getLogStartTime)
                    .orderByAsc(SysJobLogDetail::getId);
        }
        String level = StringUtils.trimToNull(query.getLogLevel());
        return Wrappers.lambdaQuery(SysJobLogDetail.class)
                .eq(query.getJobLogId() != null, SysJobLogDetail::getJobLogId, query.getJobLogId())
                .eq(level != null, SysJobLogDetail::getLogLevel, normalizeLevel(level))
                .ge(query.getLogTimeFrom() != null, SysJobLogDetail::getLogEndTime, query.getLogTimeFrom())
                .le(query.getLogTimeTo() != null, SysJobLogDetail::getLogStartTime, query.getLogTimeTo())
                .orderByAsc(SysJobLogDetail::getLogStartTime)
                .orderByAsc(SysJobLogDetail::getId);
    }

    private String normalizeLevel(String level) {
        if (StringUtils.isBlank(level)) {
            return JobLogDetailLevel.INFO;
        }
        return level.trim().toUpperCase();
    }
}
