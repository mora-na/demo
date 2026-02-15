package com.example.demo.user.dto;

import com.example.demo.dict.annotation.DictLabel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户视图对象，承载导出与关联订单的展示字段。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    private String userName;

    private String nickName;

    private String phone;

    private String email;

    @DictLabel("sys_gender")
    private String sex;

    @DictLabel("sys_status")
    private Integer status;

    private Long deptId;

    private String dataScopeType;

    private String dataScopeValue;

    private String remark;

}
