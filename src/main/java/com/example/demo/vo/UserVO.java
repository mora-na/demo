package com.example.demo.vo;

import com.example.demo.auth.model.User;
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

    @ExcelColumn(headerName = "用户名")
    private String userName;

    @ExcelColumn(headerName = "昵称")
    private String nickName;

    @ExcelColumn(headerName = "性别", mapping = {"0:女", "1:男"})
    private String sex;

    private String tst;

    @ExcelColumn(exit = false)
    private List<OrderVO> orderVOS; // 一对多

    public User toUserDTO(UserVO userVO) {
        if (userVO == null) {
            return new User();
        }
        return new User(userVO.getId(), userVO.getUserName(), userVO.getNickName(), null, userVO.getSex(), userVO.getTst());
    }

}