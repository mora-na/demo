package com.example.demo.user.service;

import com.example.demo.common.mybatis.IMppService;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.entity.User;

import java.util.List;

public interface UserService extends IMppService<User> {

    List<User> selectUsers(UserQuery query);

    User getByUserName(String userName);
}
