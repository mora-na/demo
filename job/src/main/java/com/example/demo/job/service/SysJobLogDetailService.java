package com.example.demo.job.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.job.entity.SysJobLogDetail;
import com.example.demo.job.model.JobLogDetailPart;

/**
 * 定时任务日志明细服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/21
 */
public interface SysJobLogDetailService extends IService<SysJobLogDetail> {

    void saveDetail(Long logId, JobLogDetailPart partType, String content);

    String buildDetail(Long logId, int maxLength, String separator);
}
