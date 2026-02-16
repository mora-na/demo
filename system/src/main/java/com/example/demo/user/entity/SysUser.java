package com.example.demo.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.common.annotation.Excel;
import com.example.demo.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体，映射 sys_user 表并承载基础导出字段定义。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "system.sys_user")
public class SysUser extends BaseEntity implements Serializable {

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;
    public static final int FORCE_PASSWORD_CHANGE_YES = 1;
    public static final int FORCE_PASSWORD_CHANGE_NO = 0;
    private static final long serialVersionUID = 1L;
    /**
     * 用户名（唯一）
     */
    @TableField("user_name")
    @Excel(value = "用户名", sort = 1)
    private String userName;

    /**
     * 昵称
     */
    @TableField("nick_name")
    @Excel(value = "昵称", sort = 2)
    private String nickName;

    /**
     * 手机号码
     */
    @TableField("phone")
    @Excel(value = "手机号", sort = 3)
    private String phone;

    /**
     * 用户邮箱
     */
    @TableField("email")
    @Excel(value = "邮箱", sort = 4)
    private String email;

    /**
     * 登录密码（加密存储）
     */
    @TableField("password")
    private String password;

    /**
     * 密码最近修改时间。
     */
    @TableField("password_updated_at")
    private LocalDateTime passwordUpdatedAt;

    /**
     * 是否要求用户修改密码：1-是；0-否。
     */
    @TableField("force_password_change")
    private Integer forcePasswordChange;

    /**
     * 状态：1-启用；0-禁用
     */
    @TableField("status")
    @Excel(header = "状态", mapping = {"0:禁用", "1:启用"}, sort = 5)
    private Integer status;

    /**
     * 部门ID（组织归属）
     */
    @TableField("dept_id")
    @Excel(value = "部门ID", sort = 7)
    private Long deptId;

    /**
     * 数据范围类型：ALL全量；SELF仅本人；CUSTOM自定义；NONE无数据
     */
    @TableField("data_scope_type")
    private String dataScopeType;

    /**
     * 数据范围值，CUSTOM时存储自定义范围内容（如ID列表）
     */
    @TableField("data_scope_value")
    private String dataScopeValue;

    /**
     * 性别
     */
    @TableField("sex")
    @Excel(header = "性别", mapping = {"F:女", "M:男"}, sort = 6)
    private String sex;

}
