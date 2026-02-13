package com.example.demo.job.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 定时任务更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class JobUpdateRequest {

    @Size(max = 128)
    private String name;

    @Size(max = 128)
    private String handlerName;

    @Size(max = 128)
    private String cronExpression;

    private Integer status;

    private Integer allowConcurrent;

    @Size(max = 32)
    private String misfirePolicy;

    private String params;

    @Size(max = 255)
    private String remark;
}
