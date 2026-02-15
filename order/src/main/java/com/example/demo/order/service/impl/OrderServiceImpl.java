package com.example.demo.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.annotation.DataScope;
import com.example.demo.order.dto.OrderQuery;
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
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Override
    @DataScope(permission = "order:query")
    public List<Order> getOrderListByUserId(Long id) {
        if (id == null) {
            return java.util.Collections.emptyList();
        }
        return baseMapper.selectList(Wrappers.lambdaQuery(Order.class).eq(Order::getUserId, id));
    }

    @Override
    @DataScope(permission = "order:query")
    public List<Order> selectOrders(OrderQuery query) {
        return list(buildQuery(query, true));
    }

    @Override
    @DataScope(permission = "order:query")
    public IPage<Order> selectOrdersPage(Page<Order> page, OrderQuery query) {
        if (page == null) {
            return new Page<>(1, 10);
        }
        return this.page(page, buildQuery(query, true));
    }

    @Override
    @DataScope(permission = "order:query")
    public long countOrders(OrderQuery query) {
        return count(buildQuery(query, false));
    }

    private LambdaQueryWrapper<Order> buildQuery(OrderQuery query, boolean withOrder) {
        LambdaQueryWrapper<Order> wrapper = null;
        if (query != null) {
            wrapper = Wrappers.lambdaQuery(Order.class)
                    .eq(query.getUserId() != null, Order::getUserId, query.getUserId())
                    .ge(query.getMinAmount() != null, Order::getAmount, query.getMinAmount())
                    .le(query.getMaxAmount() != null, Order::getAmount, query.getMaxAmount());
        }
        if (withOrder) {
            if (wrapper != null) {
                wrapper.orderByDesc(Order::getCreateTime)
                        .orderByDesc(Order::getId);
            }
        }
        return wrapper;
    }
}
