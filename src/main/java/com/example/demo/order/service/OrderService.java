package com.example.demo.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.order.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    List<Order> getOrderListByUserId(Long id);
}
