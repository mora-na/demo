package com.example.demo.order.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.order.entity.Order;

import java.util.List;

public interface OrderService extends IMppService<Order> {

    List<Order> getOrderListByUserId(Long id);
}
