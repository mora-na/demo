package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.entity.Order;
import com.example.demo.framework.service.impl.MppServiceImpl;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.service.OrderService;
import com.example.demo.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends MppServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public List<Order> getOrderListByUserId(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(Wrappers.lambdaQuery(Order.class).eq(Order::getUserId, id));
    }

    @Override
    public List<OrderVO> getOrderVO(List<Order> orderList) {
        if (orderList == null || orderList.isEmpty()) {
            return Collections.emptyList();
        }

        return orderList.stream().map(this::getOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderVO(Order order) {
        if (order == null) {
            return new OrderVO();
        }
        return new OrderVO(order.getId(), order.getUserId(), order.getAmount());
    }

    @Override
    public List<Order> getOrderDto(List<OrderVO> orderVOList) {
        if (orderVOList == null || orderVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return orderVOList.stream().map(this::getOrderDto).collect(Collectors.toList());
    }

    @Override
    public Order getOrderDto(OrderVO orderVO) {
        if (orderVO == null) {
            return new Order();
        }
        return new Order(orderVO.getId(), orderVO.getUserId(), orderVO.getAmount());
    }
}
