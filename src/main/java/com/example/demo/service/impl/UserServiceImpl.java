package com.example.demo.service.impl;

import com.example.demo.entity.UserDTO;
import com.example.demo.framework.service.impl.MppServiceImpl;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
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

    private final UserMapper userMapper;

    private final OrderService orderService;

    @Override
    public List<UserDTO> selectUsers(int pageNum, int pageSize, UserVO userVO) {
        try (Page<Object> ignored = PageHelper.startPage(pageNum, pageSize)) {
            return userMapper.selectUsers();
        }
    }

    @Override
    public List<UserVO> getUserVO(List<UserDTO> userDTOList) {
        if (userDTOList == null || userDTOList.isEmpty()) {
            return Collections.emptyList();
        }
        return userDTOList.stream().map(userDTO -> new UserVO(userDTO.getId(), userDTO.getName(), userDTO.getSex(), orderService.getOrderVO(userDTO.getOrderDTOS()))).collect(Collectors.toList());
    }

}
