package com.example.demo.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.annotation.MppMultiField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_user")
public class User implements Serializable {

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_name")
    @MppMultiField
    private String userName;

    @TableField("nick_name")
    @MppMultiField
    private String nickName;

    @TableField("password")
    private String password;

    @TableField("status")
    private Integer status;

    @TableField("data_scope_type")
    private String dataScopeType;

    @TableField("data_scope_value")
    private String dataScopeValue;

    @TableField("sex")
    private String sex;

    @TableField("tst")
    private String tst;

}
