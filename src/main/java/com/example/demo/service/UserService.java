package com.example.demo.service;

import com.example.demo.auth.model.User;
import com.example.demo.framework.service.IMppService;
import com.example.demo.vo.UserVO;

import java.util.List;

public interface UserService extends IMppService<User> {

    List<User> selectUsers(UserVO userVO);

    List<UserVO> getUserVO(List<User> userList);

    UserVO getUserVO(User user);

    List<User> getUserDTO(List<UserVO> userVOList);

    User getUserDTO(UserVO userVO);

    User getByUserName(String userName);
}
