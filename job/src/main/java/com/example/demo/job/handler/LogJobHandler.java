package com.example.demo.job.handler;

import com.example.demo.common.async.MdcUtils;
import com.example.demo.extension.api.handler.DynamicApiHandler;
import com.example.demo.extension.api.request.DynamicApiRequest;
import com.example.demo.job.config.JobConstants;
import com.example.demo.job.support.JobContext;
import com.example.demo.job.support.JobHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 默认日志任务处理器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Slf4j
@Component("logJobHandler")
@RequiredArgsConstructor
public class LogJobHandler implements JobHandler, DynamicApiHandler {

    private final JobConstants jobConstants;

    @Override
    public void execute(JobContext context) {
        String params = context.getParams();
        context.appendLog(jobConstants.getHandlerDemo().getManualLogStart());
        if (params != null && !params.trim().isEmpty()) {
            context.appendLog(jobConstants.getExecution().getParamsPrefix() + params);
        }

        // 情形 1：当前任务线程内日志（Quartz 任务线程） -> 一定会被收集。
        log.info("[Job] name={}, params={}", context.getJobName(), params);

        // 情形 2：new Thread 未显式透传 MDC。
        // 由于我们启用了 InheritableThreadLocal 兜底（inherit-thread-context=true），
        // 新建线程会继承父线程上下文，因此此处日志也会被收集。
        Thread plainThread = new Thread(() -> {
            log.info("[Job-NewThread] name={}, params={}", context.getJobName(), params);
            context.appendLog(jobConstants.getHandlerDemo().getNewThreadLog());
        }, jobConstants.getHandlerDemo().getPlainThreadName());
        plainThread.setDaemon(true);
        plainThread.start();

        // 情形 3：new Thread 显式透传 MDC（最稳妥，可避免线程池复用导致的上下文丢失）。
        Runnable asyncTask = MdcUtils.wrap(() -> {
            log.info("[Job-Async] name={}, async params={}", context.getJobName(), params);
            context.appendLog(jobConstants.getHandlerDemo().getAsyncThreadLog());
        });
        Thread asyncThread = MdcUtils.newThread(jobConstants.getHandlerDemo().getAsyncThreadName(), asyncTask, true);
        asyncThread.start();

        // 情形 4：自建 Executor 未包装。
        // 线程池第一次创建线程时会继承父线程上下文，所以“新建线程”阶段仍会被收集；
        // 但线程池复用后，若上下文未显式透传，后续任务可能丢失 MDC。
        ExecutorService rawExecutor = Executors.newFixedThreadPool(jobConstants.getHandlerDemo().getRawExecutorPoolSize());
        rawExecutor.submit(() -> log.info("[Job-Executor-Plain] name={}", context.getJobName()));
        rawExecutor.shutdown();

        // 情形 5：自建 Executor 显式包装（推荐，稳定收集）。
        ExecutorService wrappedExecutor = MdcUtils.wrapExecutorService(
                Executors.newFixedThreadPool(jobConstants.getHandlerDemo().getWrappedExecutorPoolSize()));
        wrappedExecutor.submit(() -> log.info("[Job-Executor-Wrapped] name={}", context.getJobName()));
        wrappedExecutor.shutdown();

        // 情形 6：ScheduledExecutor 同理（未包装也可在“首次创建线程”时继承上下文，但不稳定）。
        ScheduledExecutorService scheduled = MdcUtils.wrapScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        scheduled.schedule(
                () -> log.info("[Job-Scheduled] name={}", context.getJobName()),
                jobConstants.getHandlerDemo().getScheduleDelayMillis(),
                TimeUnit.MILLISECONDS);
        scheduled.shutdown();

        context.appendLog(jobConstants.getHandlerDemo().getManualLogEnd());
    }

    @Override
    public Object handle(DynamicApiRequest request) throws Exception {
        if (request == null) {
            log.warn("[DynamicApi] request is null");
            return Collections.singletonMap("message", "empty request");
        }

        String mode = request.getParamMode() == null ? "AUTO" : request.getParamMode().name();
        String rawBody = request.abbreviateRawBody(200);

        log.info("[DynamicApi] path={}, method={}, mode={}", request.getPath(), request.getMethod(), mode);
        log.info("[DynamicApi] pathVars={}, queryParams={}, params={}",
                request.getPathVariables(), request.getQueryParams(), request.getParams());

        if (!request.getHeaders().isEmpty()) {
            log.info("[DynamicApi] headerKeys={}", request.getHeaderKeys());
        }

        String files = request.getFileSummary();
        if (!"[]".equals(files)) {
            log.info("[DynamicApi] files={}", files);
        }

        if (rawBody != null && !rawBody.isEmpty()) {
            log.info("[DynamicApi] rawBody={}", rawBody);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "log demo ok");
        response.put("path", request.getPath());
        response.put("method", request.getMethod());
        response.put("paramMode", mode);
        response.put("pathVariables", request.getPathVariables());
        response.put("queryParams", request.getQueryParams());
        response.put("params", request.getParams());
        response.put("headerKeys", request.getHeaderKeys());
        response.put("files", files);
        response.put("rawBody", rawBody);
        response.put("note", "Authorization and sensitive headers are not logged");
        return response;
    }
}
