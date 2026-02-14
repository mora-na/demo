package com.example.demo.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.order.dto.OrderQuery;
import com.example.demo.order.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {

    List<Order> getOrderListByUserId(Long id);

    List<Order> selectOrders(OrderQuery query);

    IPage<Order> selectOrdersPage(Page<Order> page, OrderQuery query);

    long countOrders(OrderQuery query);
}
