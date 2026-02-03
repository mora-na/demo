package com.example.demo.service;

import com.example.demo.entity.UserDTO;
import com.example.demo.framework.service.IMppService;
import com.example.demo.vo.UserVO;

import java.util.List;

public interface UserService extends IMppService<UserDTO> {

    List<UserDTO> selectUsers(int pageNum, int pageSize, UserVO userVO);

    List<UserVO> getUserVO(List<UserDTO> userDTOList);

    UserVO getUserVO(UserDTO userDTO);

}
