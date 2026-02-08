package com.example.demo.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {
    private Long id;
    private String userName;
    private String nickName;
    private String dataScopeType;
    private String dataScopeValue;
}
