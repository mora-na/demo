package com.example.demo.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.mybatis.MppServiceImpl;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends MppServiceImpl<UserMapper, User> implements UserService {

    private final UserConverter userConverter;

    @Override
    public List<User> selectUsers(UserQuery query) {
        return baseMapper.selectList(Wrappers.query(userConverter.toEntity(query)));
    }

    @Override
    public User getByUserName(String userName) {
        if (userName == null) {
            return null;
        }
        return baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, userName));
    }

}
