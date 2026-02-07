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
        UserQuery query = new UserQuery(1L, "alice", "Ali", "F", "note");
        User user = converter.toEntity(query);

        assertEquals(query.getId(), user.getId());
        assertEquals(query.getUserName(), user.getUserName());
        assertEquals(query.getNickName(), user.getNickName());
    }

    @Test
    void toEntity_fromVo() {
        UserConverter converter = new UserConverter();
        UserVO vo = new UserVO(2L, "bob", "B", "M", "memo", Collections.emptyList());
        User user = converter.toEntity(vo);

        assertEquals(vo.getUserName(), user.getUserName());
        assertEquals(vo.getNickName(), user.getNickName());
    }

    @Test
    void toView_includesOrders() {
        UserConverter converter = new UserConverter();
        User user = new User(1L, "alice", "Ali", null, "F", "note");
        OrderVO orderVO = new OrderVO(10L, 1L, java.math.BigDecimal.ONE);

        UserVO view = converter.toView(user, Collections.singletonList(orderVO));
        assertEquals(1, view.getOrderVOS().size());
    }

    @Test
    void toEntityList_handlesNull() {
        UserConverter converter = new UserConverter();
        assertTrue(converter.toEntityList(null).isEmpty());
    }
}
