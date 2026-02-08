package com.example.demo.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String userName;

    private String nickName;

    private String sex;

    private String tst;

    private Integer status;
}
