package com.example.demo.order.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 订单创建请求。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Data
public class OrderCreateRequest {

    @NotNull
    private Long userId;

    @NotNull
    private BigDecimal amount;

    private String remark;
}
