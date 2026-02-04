package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
    public List<OrderDTO> getOrderListByUserId(Long id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return baseMapper.selectList(Wrappers.lambdaQuery(OrderDTO.class).eq(OrderDTO::getUserId, id));
    }

    @Override
    public List<OrderVO> getOrderVO(List<OrderDTO> orderDTOList) {
        if (orderDTOList == null || orderDTOList.isEmpty()) {
            return Collections.emptyList();
        }

        return orderDTOList.stream().map(this::getOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderVO(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return new OrderVO();
        }
        return new OrderVO(orderDTO.getId(), orderDTO.getUserId(), orderDTO.getAmount());
    }

    @Override
    public List<OrderDTO> getOrderDto(List<OrderVO> orderVOList) {
        if (orderVOList == null || orderVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return orderVOList.stream().map(this::getOrderDto).collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderDto(OrderVO orderVO) {
        if (orderVO == null) {
            return new OrderDTO();
        }
        return new OrderDTO(orderVO.getId(), orderVO.getUserId(), orderVO.getAmount());
    }
}
