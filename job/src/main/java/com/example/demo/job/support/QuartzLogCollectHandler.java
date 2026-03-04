package com.example.demo.job.support;

import com.example.demo.job.api.JobContext;
import com.example.demo.job.service.SysJobLogDetailService;
import com.logcollect.api.handler.LogCollectHandler;
import com.logcollect.api.model.AggregatedLog;
import com.logcollect.api.model.LogCollectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuartzLogCollectHandler implements LogCollectHandler {

    private final SysJobLogDetailService sysJobLogDetailService;

    @Override
    public void before(LogCollectContext context) {
        context.setBusinessId(((JobContext) context.getMethodArgs()[0]).getExecutionLogId());
    }

    @Override
    public void flushAggregatedLog(LogCollectContext context, AggregatedLog aggregatedLog) {
        sysJobLogDetailService.appendDetail(context.getBusinessId(Long.class),
                aggregatedLog.getMaxLevel(),
                aggregatedLog.getFirstLogTime(),
                aggregatedLog.getLastLogTime(),
                aggregatedLog.getContent());
    }

    @Override
    public void after(LogCollectContext context) {
    }


}
