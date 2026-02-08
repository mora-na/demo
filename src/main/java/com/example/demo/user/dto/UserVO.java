package com.example.demo.user.dto;

import com.example.demo.common.annotation.ExcelColumn;
import com.example.demo.order.dto.OrderVO;
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

    @ExcelColumn(headerName = "状态", mapping = {"0:禁用", "1:启用"})
    private Integer status;

    @ExcelColumn(exit = false)
    private String dataScopeType;

    @ExcelColumn(exit = false)
    private String dataScopeValue;

    private String tst;

    @ExcelColumn(exit = false)
    private List<OrderVO> orderVOS; // 一对多

}
