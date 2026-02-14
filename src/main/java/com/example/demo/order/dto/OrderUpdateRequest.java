package com.example.demo.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单更新请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class OrderUpdateRequest {

    private Long userId;

    private BigDecimal amount;

    private String remark;
}
