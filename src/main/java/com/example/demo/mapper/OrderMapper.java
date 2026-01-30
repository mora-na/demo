package com.example.demo.mapper;

import com.example.demo.entity.OrderDTO;
import com.example.demo.framework.service.MppBaseMapper;

import java.util.List;

public interface OrderMapper extends MppBaseMapper<OrderDTO> {

    List<OrderDTO> selectByUserId(Long userId);

}
