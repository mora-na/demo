package com.example.demo.extension.executor;

import com.example.demo.extension.config.DynamicApiConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 动态接口执行器。
 */
@Slf4j
@Component
public class DynamicApiExecutor {

    private final ThreadPoolTaskExecutor executor;
    private final ExecuteStrategyFactory strategyFactory;
    private final DynamicApiConstants constants;

    public DynamicApiExecutor(@Qualifier("dynamicApiTaskExecutor") ThreadPoolTaskExecutor executor,
                              ExecuteStrategyFactory strategyFactory,
                              DynamicApiConstants constants) {
        this.executor = executor;
        this.strategyFactory = strategyFactory;
        this.constants = constants;
    }

    public CompletableFuture<DynamicApiExecuteResult> executeAsync(DynamicApiContext context) {
        if (context == null || context.getMeta() == null) {
            return CompletableFuture.completedFuture(
                    DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                            constants.getMessage().getExecuteFailed()));
        }
        String type = context.getMeta().getType();
        ExecuteStrategy strategy = strategyFactory.get(type);
        if (strategy == null) {
            return CompletableFuture.completedFuture(
                    DynamicApiExecuteResult.error(constants.getController().getBadRequestCode(),
                            constants.getMessage().getTypeInvalid()));
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return strategy.execute(context);
            } catch (Exception ex) {
                String traceId = MDC.get("traceId");
                log.error("Dynamic api execute failed: apiId={}, path={}, method={}, type={}, traceId={}",
                        context.getMeta().getApi().getId(),
                        context.getMeta().getApi().getPath(),
                        context.getMeta().getApi().getMethod(),
                        context.getMeta().getType(),
                        traceId,
                        ex);
                return DynamicApiExecuteResult.error(constants.getController().getInternalServerErrorCode(),
                        constants.getMessage().getExecuteFailed());
            }
        }, executor);
    }
}
