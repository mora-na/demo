package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.entity.UserDTO;
import com.example.demo.framework.service.impl.MppServiceImpl;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends MppServiceImpl<UserMapper, UserDTO> implements UserService {

    private final OrderService orderService;

    @Override
    public List<UserDTO> selectUsers(UserVO userVO) {
        return baseMapper.selectList(Wrappers.query(getUserDTO(userVO)));
    }

    @Override
    public List<UserVO> getUserVO(List<UserDTO> userDTOList) {
        if (userDTOList == null || userDTOList.isEmpty()) {
            return Collections.emptyList();
        }
        return userDTOList.stream().map(userDTO -> new UserVO(userDTO.getId(), userDTO.getName(), userDTO.getSex(), userDTO.getTst(), orderService.getOrderVO(orderService.getOrderListByUserId(userDTO.getId())))).collect(Collectors.toList());
    }

    @Override
    public UserVO getUserVO(UserDTO userDTO) {
        if (userDTO == null) {
            return new UserVO();
        }
        return new UserVO(userDTO.getId(), userDTO.getName(), userDTO.getSex(), userDTO.getTst(), orderService.getOrderVO(orderService.getOrderListByUserId(userDTO.getId())));
    }

    @Override
    public List<UserDTO> getUserDTO(List<UserVO> userVOList) {
        if (userVOList == null || userVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return userVOList.stream().map(this::getUserDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserDTO(UserVO userVO) {
        if (userVO == null) {
            return new UserDTO();
        }
        return new UserDTO(userVO.getId(), userVO.getName(), userVO.getSex(), userVO.getTst());
    }

}
