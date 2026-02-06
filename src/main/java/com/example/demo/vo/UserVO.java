package com.example.demo.vo;

import com.example.demo.entity.UserDTO;
import com.example.demo.framework.tools.ExcelColumn;
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

    @ExcelColumn(exit = false)
    private Long id;

    @ExcelColumn(headerName = "姓名")
    private String name;

    @ExcelColumn(headerName = "性别", mapping = {"0:女", "1:男"})
    private String sex;

    private String tst;

    @ExcelColumn(exit = false)
    private List<OrderVO> orderVOS; // 一对多

    public UserDTO toUserDTO(UserVO userVO) {
        if (userVO == null) {
            return new UserDTO();
        }
        return new UserDTO(userVO.getId(), userVO.getName(), userVO.getSex(), userVO.getTst());
    }

}