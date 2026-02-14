package com.example.demo.log.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 操作日志查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class OperLogQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName;

    private String title;

    private Integer status;

    private Integer businessType;

    private String beginTime;

    private String endTime;
}
