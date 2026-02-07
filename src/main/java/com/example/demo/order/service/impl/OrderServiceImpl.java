package com.example.demo.order.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.order.entity.Order;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends MppServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    public List<Order> getOrderListByUserId(Long id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return baseMapper.selectList(Wrappers.lambdaQuery(Order.class).eq(Order::getUserId, id));
    }
}
