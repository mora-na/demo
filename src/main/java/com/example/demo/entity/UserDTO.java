package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.demo.vo.OrderVO;
import com.example.demo.vo.UserVO;
import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_user")
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("name")
    @MppMultiId
    private String name;

    @TableField("sex")
    @MppMultiId
    private String sex;

    @TableField("tst")
    private String tst;

    public UserVO toUserVO(UserDTO userDTO, List<OrderVO> orderVOList) {
        if (userDTO == null) {
            return new UserVO();
        }
        return new UserVO(userDTO.getId(), userDTO.getName(), userDTO.getSex(), userDTO.getTst(), orderVOList);
    }

}