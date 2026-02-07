package com.example.demo.service;

import com.example.demo.entity.Order;
import com.example.demo.framework.service.IMppService;
import com.example.demo.vo.OrderVO;

import java.util.List;

public interface OrderService extends IMppService<Order> {

    List<Order> getOrderListByUserId(Long id);

    List<OrderVO> getOrderVO(List<Order> orderList);

    OrderVO getOrderVO(Order order);

    List<Order> getOrderDto(List<OrderVO> orderVOList);

    Order getOrderDto(OrderVO orderVO);
}
