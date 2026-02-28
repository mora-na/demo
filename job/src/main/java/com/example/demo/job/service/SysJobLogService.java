package com.example.demo.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.job.api.JobContext;
import com.example.demo.job.dto.JobLogQuery;
import com.example.demo.job.dto.JobLogVO;
import com.example.demo.job.entity.SysJobLog;
import org.quartz.JobExecutionContext;

import java.time.LocalDateTime;

/**
 * 定时任务执行记录服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
public interface SysJobLogService extends IService<SysJobLog> {

    IPage<SysJobLog> selectJobLogsPage(Page<SysJobLog> page, JobLogQuery query);

    SysJobLog createStartLog(JobExecutionContext context, JobContext jobContext, LocalDateTime startTime);

    void markSuccess(SysJobLog log, LocalDateTime endTime);

    void markFailure(SysJobLog log, LocalDateTime endTime, String errorMessage, String errorStacktrace);

    JobLogVO toView(SysJobLog log);
}
