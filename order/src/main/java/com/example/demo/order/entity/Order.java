package com.example.demo.order.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "\"order\".sys_order")
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 下单用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单金额
     */
    @TableField("amount")
    private BigDecimal amount;

}
