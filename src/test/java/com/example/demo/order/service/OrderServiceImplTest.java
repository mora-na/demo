package com.example.demo.order.service;

import com.example.demo.order.entity.Order;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    @Test
    void getOrderListByUserId_returnsEmptyWhenNull() {
        OrderServiceImpl service = new OrderServiceImpl();
        assertTrue(service.getOrderListByUserId(null).isEmpty());
    }

    @Test
    void getOrderListByUserId_usesMapper() {
        OrderServiceImpl service = new OrderServiceImpl();
        OrderMapper mapper = mock(OrderMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        Order order = new Order(1L, 2L, BigDecimal.TEN);
        when(mapper.selectList(any())).thenReturn(Collections.singletonList(order));

        assertEquals(1, service.getOrderListByUserId(2L).size());
    }
}
