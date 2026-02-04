package com.example.demo.vo;

import com.example.demo.entity.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String sex;

    private String tst;

    private List<OrderVO> orderVOS; // 一对多

    public UserDTO toUserDTO(UserVO userVO) {
        if (userVO == null) {
            return new UserDTO();
        }
        return new UserDTO(userVO.getId(), userVO.getName(), userVO.getSex(), userVO.getTst());
    }

}