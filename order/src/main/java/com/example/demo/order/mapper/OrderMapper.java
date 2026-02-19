package com.example.demo.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OrderMapper extends BaseMapper<Order> {

//    List<OrderDTO> selectByUserId(Long userId);

}
