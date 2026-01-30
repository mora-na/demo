package com.example.demo.mapper;

import com.example.demo.entity.UserDTO;
import com.example.demo.framework.service.MppBaseMapper;

import java.util.List;

public interface UserMapper extends MppBaseMapper<UserDTO> {

    UserDTO selectUserById(Integer userId);

    List<UserDTO> selectUsers();

}
