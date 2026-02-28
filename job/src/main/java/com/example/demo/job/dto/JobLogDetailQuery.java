package com.example.demo.job.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务执行明细日志查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobLogDetailQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long jobLogId;

    private String logLevel;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logTimeFrom;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime logTimeTo;
}
