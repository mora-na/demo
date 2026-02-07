package com.example.demo.user.service;

import com.example.demo.order.converter.OrderConverter;
import com.example.demo.order.entity.Order;
import com.example.demo.order.service.OrderService;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserViewService {

    private final OrderService orderService;
    private final OrderConverter orderConverter;
    private final UserConverter userConverter;

    public UserVO toView(User user) {
        if (user == null) {
            return new UserVO();
        }
        List<Order> orders = orderService.getOrderListByUserId(user.getId());
        return userConverter.toView(user, orderConverter.toViewList(orders));
    }

    public List<UserVO> toViewList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(this::toView).collect(Collectors.toList());
    }
}
