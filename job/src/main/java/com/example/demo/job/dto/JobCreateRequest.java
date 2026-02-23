package com.example.demo.job.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 定时任务创建请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
public class JobCreateRequest {

    @NotBlank
    @Size(max = 128)
    private String name;

    @NotBlank
    @Size(max = 128)
    private String handlerName;

    @NotBlank
    @Size(max = 128)
    private String cronExpression;

    private Integer status;

    private Integer allowConcurrent;

    @Size(max = 32)
    private String misfirePolicy;

    private String params;

    @Size(max = 16)
    private String logCollectLevel;

    @Size(max = 255)
    private String remark;
}
