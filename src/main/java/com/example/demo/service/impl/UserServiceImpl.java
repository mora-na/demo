package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.entity.User;
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
public class UserServiceImpl extends MppServiceImpl<UserMapper, User> implements UserService {

    private final OrderService orderService;

    @Override
    public List<User> selectUsers(UserVO userVO) {
        return baseMapper.selectList(Wrappers.query(getUserDTO(userVO)));
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (userList == null || userList.isEmpty()) {
            return Collections.emptyList();
        }
        return userList.stream().map(userDTO -> new UserVO(userDTO.getId(), userDTO.getUserName(), userDTO.getNickName(), userDTO.getSex(), userDTO.getTst(), orderService.getOrderVO(orderService.getOrderListByUserId(userDTO.getId())))).collect(Collectors.toList());
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return new UserVO();
        }
        return new UserVO(user.getId(), user.getUserName(), user.getNickName(), user.getSex(), user.getTst(), orderService.getOrderVO(orderService.getOrderListByUserId(user.getId())));
    }

    @Override
    public List<User> getUserDTO(List<UserVO> userVOList) {
        if (userVOList == null || userVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return userVOList.stream().map(this::getUserDTO).collect(Collectors.toList());
    }

    @Override
    public User getUserDTO(UserVO userVO) {
        if (userVO == null) {
            return new User();
        }
        return new User(userVO.getId(), userVO.getUserName(), userVO.getNickName(), null, userVO.getSex(), userVO.getTst());
    }

    @Override
    public User getByUserName(String userName) {
        if (userName == null) {
            return null;
        }
        return baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
    }

}
