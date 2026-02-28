package com.example.demo.job.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.job.dto.JobLogDetailQuery;
import com.example.demo.job.dto.JobLogDetailVO;
import com.example.demo.job.entity.SysJobLogDetail;

import java.time.LocalDateTime;

/**
 * 定时任务执行明细日志服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
public interface SysJobLogDetailService extends IService<SysJobLogDetail> {

    IPage<SysJobLogDetail> selectJobLogDetailsPage(Page<SysJobLogDetail> page, JobLogDetailQuery query);

    SysJobLogDetail appendDetail(Long jobLogId,
                                 String logLevel,
                                 LocalDateTime logStartTime,
                                 LocalDateTime logEndTime,
                                 String logContent);

    JobLogDetailVO toView(SysJobLogDetail detail);
}
