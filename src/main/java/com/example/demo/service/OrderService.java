package com.example.demo.service;

import com.example.demo.entity.OrderDTO;
import com.example.demo.framework.service.IMppService;
import com.example.demo.vo.OrderVO;

import java.util.List;

public interface OrderService extends IMppService<OrderDTO> {

    List<OrderDTO> getOrderListByUserId(Long id);

    List<OrderVO> getOrderVO(List<OrderDTO> orderDTOList);

    OrderVO getOrderVO(OrderDTO orderDTO);

    List<OrderDTO> getOrderDto(List<OrderVO> orderVOList);

    OrderDTO getOrderDto(OrderVO orderVO);
}
