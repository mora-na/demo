package com.example.demo.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单查询参数。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class OrderQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;
}
