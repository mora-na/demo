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
        return new User(query.getId(), query.getUserName(), query.getNickName(), null, query.getSex(), query.getTst());
    }

    public User toEntity(UserVO userVO) {
        if (userVO == null) {
            return new User();
        }
        return new User(userVO.getId(), userVO.getUserName(), userVO.getNickName(), null, userVO.getSex(), userVO.getTst());
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
        return new UserVO(user.getId(), user.getUserName(), user.getNickName(), user.getSex(), user.getTst(), orderVOList);
    }
}
