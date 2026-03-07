package com.example.demo.job.handler.support;

import com.example.demo.job.api.JobContext;
import com.example.demo.job.handler.AsyncLogTestJobHandler;
import com.example.demo.job.support.QuartzLogCollectHandler;
import com.logcollect.api.annotation.LogCollect;
import com.logcollect.core.context.LogCollectContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Nested @LogCollect probe used by {@link AsyncLogTestJobHandler}.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/7
 */
@Slf4j
@Component
public class AsyncNestedLogCollectProbe {

    @LogCollect(handler = QuartzLogCollectHandler.class, minLevel = "DEBUG")
    public void nestedLog(JobContext context, String runId) {
        log.info("[AsyncJobTest][{}][{}] {} | thread={} | inCollectContext={}",
                runId,
                AsyncLogScenario.NESTED_LOGCOLLECT.stage(),
                AsyncLogScenario.NESTED_LOGCOLLECT.message("nestedLogCollectProbe.nestedLog(...)"),
                Thread.currentThread().getName(),
                LogCollectContextUtils.isInLogCollectContext());
    }
}
