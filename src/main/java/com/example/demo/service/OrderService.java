package com.example.demo.service;

import com.example.demo.entity.OrderDTO;
import com.example.demo.framework.service.IMppService;
import com.example.demo.vo.OrderVO;

import java.util.List;

public interface OrderService extends IMppService<OrderDTO> {

    List<OrderVO> getOrderVO(List<OrderDTO> orderDTOList);

}
