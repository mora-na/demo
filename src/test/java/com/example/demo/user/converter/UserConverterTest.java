package com.example.demo.user.converter;

import com.example.demo.order.dto.OrderVO;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserConverterTest {

    @Test
    void toEntity_fromQuery() {
        UserConverter converter = new UserConverter();
        UserQuery query = new UserQuery();
        query.setId(1L);
        query.setUserName("alice");
        query.setNickName("Ali");
        query.setSex("F");
        query.setTst("note");
        query.setDeptId(9L);
        User user = converter.toEntity(query);

        assertEquals(query.getId(), user.getId());
        assertEquals(query.getUserName(), user.getUserName());
        assertEquals(query.getNickName(), user.getNickName());
        assertEquals(query.getDeptId(), user.getDeptId());
    }

    @Test
    void toEntity_fromVo() {
        UserConverter converter = new UserConverter();
        UserVO vo = new UserVO();
        vo.setId(2L);
        vo.setUserName("bob");
        vo.setNickName("B");
        vo.setSex("M");
        vo.setTst("memo");
        vo.setDeptId(5L);
        vo.setOrderVOS(Collections.emptyList());
        User user = converter.toEntity(vo);

        assertEquals(vo.getUserName(), user.getUserName());
        assertEquals(vo.getNickName(), user.getNickName());
        assertEquals(vo.getDeptId(), user.getDeptId());
    }

    @Test
    void toView_includesOrders() {
        UserConverter converter = new UserConverter();
        User user = new User();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Ali");
        user.setSex("F");
        user.setTst("note");
        user.setDeptId(2L);
        OrderVO orderVO = new OrderVO(10L, 1L, java.math.BigDecimal.ONE);

        UserVO view = converter.toView(user, Collections.singletonList(orderVO));
        assertEquals(1, view.getOrderVOS().size());
        assertEquals(user.getDeptId(), view.getDeptId());
    }

    @Test
    void toEntityList_handlesNull() {
        UserConverter converter = new UserConverter();
        assertTrue(converter.toEntityList(null).isEmpty());
    }
}
