package com.example.demo.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行记录查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTimeTo;

    private Integer status;

    private String triggerType;
}
