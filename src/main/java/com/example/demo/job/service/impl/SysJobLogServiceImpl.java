package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.job.dto.JobLogQuery;
import com.example.demo.job.dto.JobLogVO;
import com.example.demo.job.entity.SysJobLog;
import com.example.demo.job.mapper.SysJobLogMapper;
import com.example.demo.job.service.SysJobLogService;
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
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    @Override
    public List<SysJobLog> selectLogs(JobLogQuery query) {
        if (query == null) {
            return list(Wrappers.lambdaQuery(SysJobLog.class)
                    .orderByDesc(SysJobLog::getStartTime)
                    .orderByDesc(SysJobLog::getId));
        }
        return list(Wrappers.lambdaQuery(SysJobLog.class)
                .eq(query.getJobId() != null, SysJobLog::getJobId, query.getJobId())
                .orderByDesc(SysJobLog::getStartTime)
                .orderByDesc(SysJobLog::getId));
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
}
