package com.example.demo.job.handler.support;

import com.logcollect.core.context.LogCollectContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Probe for @Async + custom AsyncConfigurer path.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/3/7
 */
@Slf4j
@Component
@Profile("job-async-custom")
public class AsyncLogCustomConfigurerProbe {

    @Async
    public CompletableFuture<Void> asyncLog(String runId, AsyncLogScenario scenario, String implementation) {
        log.info("[AsyncJobTest][{}][{}] {} | thread={} | inCollectContext={}",
                runId,
                scenario.stage(),
                scenario.message(implementation),
                Thread.currentThread().getName(),
                LogCollectContextUtils.isInLogCollectContext());
        return CompletableFuture.completedFuture(null);
    }
}
