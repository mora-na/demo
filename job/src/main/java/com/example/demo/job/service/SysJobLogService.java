package com.example.demo.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.job.dto.JobLogDetailVO;
import com.example.demo.job.dto.JobLogQuery;
import com.example.demo.job.dto.JobLogVO;
import com.example.demo.job.entity.SysJobLog;

import java.util.List;

/**
 * 定时任务日志服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface SysJobLogService extends IService<SysJobLog> {

    List<SysJobLog> selectLogs(JobLogQuery query);

    IPage<SysJobLog> selectLogsPage(Page<SysJobLog> page, JobLogQuery query);

    List<JobLogVO> toViewList(List<SysJobLog> logs);

    JobLogDetailVO toDetailView(SysJobLog log);
}
