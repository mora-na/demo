package com.example.demo.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.annotation.ExcelColumn;
import com.example.demo.common.annotation.MppMultiField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户实体，映射 sys_user 表并承载基础导出字段定义。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_user")
public class User implements Serializable {

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    private static final long serialVersionUID = 1L;
    /**
     * 主键ID
     */
    @ExcelColumn(exit = false)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    @TableField("user_name")
    @MppMultiField
    @ExcelColumn(headerName = "用户名")
    private String userName;

    /**
     * 昵称
     */
    @TableField("nick_name")
    @MppMultiField
    @ExcelColumn(headerName = "昵称")
    private String nickName;

    /**
     * 登录密码（加密存储）
     */
    @TableField("password")
    @ExcelColumn(exit = false)
    private String password;

    /**
     * 状态：1-启用；0-禁用
     */
    @TableField("status")
    @ExcelColumn(headerName = "状态", mapping = {"0:禁用", "1:启用"})
    private Integer status;

    /**
     * 数据范围类型：ALL全量；SELF仅本人；CUSTOM自定义；NONE无数据
     */
    @TableField("data_scope_type")
    @ExcelColumn(exit = false)
    private String dataScopeType;

    /**
     * 数据范围值，CUSTOM时存储自定义范围内容（如ID列表）
     */
    @TableField("data_scope_value")
    @ExcelColumn(exit = false)
    private String dataScopeValue;

    /**
     * 性别
     */
    @TableField("sex")
    @ExcelColumn(headerName = "性别", mapping = {"0:女", "1:男"})
    private String sex;

    /**
     * 备注/测试字段
     */
    @TableField("tst")
    private String tst;

}
