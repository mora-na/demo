package com.example.demo.user.service;

import com.example.demo.order.converter.OrderConverter;
import com.example.demo.order.entity.Order;
import com.example.demo.order.service.OrderService;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserViewServiceTest {

    @Test
    void toView_buildsOrderViews() {
        OrderService orderService = Mockito.mock(OrderService.class);
        OrderConverter orderConverter = new OrderConverter();
        UserConverter userConverter = new UserConverter();
        UserViewService viewService = new UserViewService(orderService, orderConverter, userConverter);

        User user = new User();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Ali");
        user.setSex("F");
        user.setTst("note");
        when(orderService.getOrderListByUserId(1L))
                .thenReturn(Collections.singletonList(new Order(10L, 1L, BigDecimal.ONE)));

        UserVO view = viewService.toView(user);
        assertEquals(1, view.getOrderVOS().size());
    }
}
