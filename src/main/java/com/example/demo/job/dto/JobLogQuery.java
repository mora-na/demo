package com.example.demo.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 定时任务日志查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobId;
}
