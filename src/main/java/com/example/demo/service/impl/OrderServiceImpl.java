package com.example.demo.service.impl;

import com.example.demo.entity.OrderDTO;
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
public class OrderServiceImpl extends MppServiceImpl<OrderMapper, OrderDTO> implements OrderService {
    @Override
    public List<OrderVO> getOrderVO(List<OrderDTO> orderDTOList) {
        if (orderDTOList == null || orderDTOList.isEmpty()) {
            return Collections.emptyList();
        }

        return orderDTOList.stream().map(orderDTO -> new OrderVO(orderDTO.getId(), orderDTO.getUserId(), orderDTO.getAmount())).collect(Collectors.toList());
    }
}
