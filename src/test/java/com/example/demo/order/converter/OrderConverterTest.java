package com.example.demo.order.converter;

import com.example.demo.order.dto.OrderVO;
import com.example.demo.order.entity.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderConverterTest {

    @Test
    void toViewAndToEntity_roundTrip() {
        OrderConverter converter = new OrderConverter();
        Order order = new Order(1L, 2L, new BigDecimal("10.50"));

        OrderVO vo = converter.toView(order);
        assertEquals(order.getId(), vo.getId());
        assertEquals(order.getUserId(), vo.getUserId());
        assertEquals(order.getAmount(), vo.getAmount());

        Order entity = converter.toEntity(vo);
        assertEquals(order.getId(), entity.getId());
        assertEquals(order.getUserId(), entity.getUserId());
        assertEquals(order.getAmount(), entity.getAmount());
    }

    @Test
    void toViewList_handlesNullOrEmpty() {
        OrderConverter converter = new OrderConverter();
        assertTrue(converter.toViewList(null).isEmpty());
        assertEquals(2, converter.toViewList(Arrays.asList(
                new Order(1L, 1L, BigDecimal.ONE),
                new Order(2L, 1L, BigDecimal.TEN)
        )).size());
    }
}
