package com.example.demo.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.framework.annotation.MppMultiField;
import com.example.demo.vo.OrderVO;
import com.example.demo.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_user")
public class User implements Serializable {

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

    @TableField("sex")
    private String sex;

    @TableField("tst")
    private String tst;

    public UserVO toUserVO(User user, List<OrderVO> orderVOList) {
        if (user == null) {
            return new UserVO();
        }
        return new UserVO(user.getId(), user.getUserName(), user.getNickName(), user.getSex(), user.getTst(), orderVOList);
    }

}
