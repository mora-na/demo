package com.example.demo.job.handler;

import com.example.demo.job.support.JobContext;
import com.example.demo.job.support.JobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 默认日志任务处理器。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Slf4j
@Component("logJobHandler")
public class LogJobHandler implements JobHandler {

    @Override
    public void execute(JobContext context) {
        log.info("[Job] name={}, params={}",
                context.getJobName(),
                context.getParams());
    }
}
