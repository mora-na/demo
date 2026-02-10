package com.example.demo.job;

import org.springframework.stereotype.Component;
import tech.powerjob.worker.annotation.PowerJobHandler;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.log.OmsLogger;

@Component
public class DemoPowerJobProcessor {

    @PowerJobHandler(name = "demoJob")
    public String demoJob(TaskContext context) {
        OmsLogger logger = context.getOmsLogger();
        logger.info("PowerJob demo start. jobId={}, instanceId={}, params={}",
                context.getJobId(), context.getInstanceId(), context.getJobParams());
        return "done";
    }
}
