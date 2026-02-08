package com.example.demo.user.converter;

import com.example.demo.order.dto.OrderVO;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserConverter {

    public User toEntity(UserQuery query) {
        if (query == null) {
            return new User();
        }
        User user = new User();
        user.setId(query.getId());
        user.setUserName(query.getUserName());
        user.setNickName(query.getNickName());
        user.setSex(query.getSex());
        user.setTst(query.getTst());
        user.setStatus(query.getStatus());
        return user;
    }

    public User toEntity(UserVO userVO) {
        if (userVO == null) {
            return new User();
        }
        User user = new User();
        user.setId(userVO.getId());
        user.setUserName(userVO.getUserName());
        user.setNickName(userVO.getNickName());
        user.setSex(userVO.getSex());
        user.setStatus(userVO.getStatus());
        user.setDataScopeType(userVO.getDataScopeType());
        user.setDataScopeValue(userVO.getDataScopeValue());
        user.setTst(userVO.getTst());
        return user;
    }

    public List<User> toEntityList(List<UserVO> userVOList) {
        if (userVOList == null || userVOList.isEmpty()) {
            return Collections.emptyList();
        }
        return userVOList.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public UserVO toView(User user, List<OrderVO> orderVOList) {
        if (user == null) {
            return new UserVO();
        }
        UserVO view = new UserVO();
        view.setId(user.getId());
        view.setUserName(user.getUserName());
        view.setNickName(user.getNickName());
        view.setSex(user.getSex());
        view.setStatus(user.getStatus());
        view.setDataScopeType(user.getDataScopeType());
        view.setDataScopeValue(user.getDataScopeValue());
        view.setTst(user.getTst());
        view.setOrderVOS(orderVOList);
        return view;
    }
}
