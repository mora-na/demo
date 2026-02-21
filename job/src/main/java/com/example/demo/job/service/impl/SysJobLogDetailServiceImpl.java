package com.example.demo.job.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.job.entity.SysJobLogDetail;
import com.example.demo.job.mapper.SysJobLogDetailMapper;
import com.example.demo.job.model.JobLogDetailPart;
import com.example.demo.job.service.SysJobLogDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务日志明细服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysJobLogDetailServiceImpl extends ServiceImpl<SysJobLogDetailMapper, SysJobLogDetail>
        implements SysJobLogDetailService {

    @Override
    public void saveDetail(Long logId, JobLogDetailPart partType, String content) {
        if (logId == null || partType == null || StringUtils.isBlank(content)) {
            return;
        }
        if (exists(logId, partType)) {
            return;
        }
        SysJobLogDetail detail = new SysJobLogDetail();
        detail.setLogId(logId);
        detail.setPartType(partType.name());
        detail.setLogDetail(content);
        try {
            save(detail);
        } catch (Exception ex) {
            if (log.isDebugEnabled()) {
                log.debug("Job log detail insert failed: logId={}, partType={}, error={}",
                        logId, partType.name(), ex.getMessage());
            }
        }
    }

    @Override
    public String buildDetail(Long logId, int maxLength, String separator) {
        if (logId == null) {
            return null;
        }
        List<SysJobLogDetail> details = lambdaQuery()
                .eq(SysJobLogDetail::getLogId, logId)
                .list();
        if (details == null || details.isEmpty()) {
            return null;
        }
        String manual = null;
        String auto = null;
        for (SysJobLogDetail detail : details) {
            if (detail == null || detail.getPartType() == null) {
                continue;
            }
            if (JobLogDetailPart.MANUAL.name().equals(detail.getPartType())) {
                manual = detail.getLogDetail();
            } else if (JobLogDetailPart.AUTO.name().equals(detail.getPartType())) {
                auto = detail.getLogDetail();
            }
        }
        String merged = merge(manual, auto, separator);
        return trim(merged, maxLength);
    }

    private boolean exists(Long logId, JobLogDetailPart partType) {
        return lambdaQuery()
                .eq(SysJobLogDetail::getLogId, logId)
                .eq(SysJobLogDetail::getPartType, partType.name())
                .count() > 0;
    }

    private String merge(String manual, String auto, String separator) {
        String left = manual == null ? "" : manual.trim();
        String right = auto == null ? "" : auto.trim();
        if (left.isEmpty()) {
            return right.isEmpty() ? null : right;
        }
        if (right.isEmpty()) {
            return left;
        }
        return left + (separator == null ? "" : separator) + right;
    }

    private String trim(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (maxLength <= 0) {
            return value;
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
