package com.example.demo.job.api;

/**
 * Job handler contract.
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
public interface JobHandler {

    void execute(JobContext context) throws Exception;
}
