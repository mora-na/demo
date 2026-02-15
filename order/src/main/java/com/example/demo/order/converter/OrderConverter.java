package com.example.demo.order.converter;

import com.example.demo.order.dto.OrderVO;
import com.example.demo.order.entity.Order;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderConverter {

    public OrderVO toView(Order order) {
        if (order == null) {
            return new OrderVO();
        }
        OrderVO view = new OrderVO();
        view.setId(order.getId());
        view.setUserId(order.getUserId());
        view.setAmount(order.getAmount());
        view.setRemark(order.getRemark());
        view.setCreatedAt(order.getCreateTime());
        return view;
    }

    public List<OrderVO> toViewList(List<Order> orderList) {
        if (orderList == null || orderList.isEmpty()) {
            return Collections.emptyList();
        }
        return orderList.stream().map(this::toView).collect(Collectors.toList());
    }

    public Order toEntity(OrderVO orderVO) {
        if (orderVO == null) {
            return new Order();
        }
        Order order = new Order();
        order.setId(orderVO.getId());
        order.setUserId(orderVO.getUserId());
        order.setAmount(orderVO.getAmount());
        order.setRemark(orderVO.getRemark());
        return order;
    }

    public List<Order> toEntityList(List<OrderVO> orderVOList) {
        if (orderVOList == null || orderVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return orderVOList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
