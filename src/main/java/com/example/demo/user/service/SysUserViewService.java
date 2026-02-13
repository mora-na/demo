package com.example.demo.user.service;

import com.example.demo.order.converter.OrderConverter;
import com.example.demo.order.entity.Order;
import com.example.demo.order.service.OrderService;
import com.example.demo.user.converter.SysUserConverter;
import com.example.demo.user.dto.SysUserVO;
import com.example.demo.user.entity.SysUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户视图装配服务，聚合订单信息后生成用户视图对象。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Service
@RequiredArgsConstructor
public class SysUserViewService {

    private final OrderService orderService;
    private final OrderConverter orderConverter;
    private final SysUserConverter userConverter;

    public SysUserVO toView(SysUser user) {
        if (user == null) {
            return new SysUserVO();
        }
        List<Order> orders = orderService.getOrderListByUserId(user.getId());
        return userConverter.toView(user, orderConverter.toViewList(orders));
    }

    public List<SysUserVO> toViewList(List<SysUser> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream().map(this::toView).collect(Collectors.toList());
    }
}
