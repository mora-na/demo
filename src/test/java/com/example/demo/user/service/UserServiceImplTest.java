package com.example.demo.user.service;

import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Test
    void selectUsers_usesMapper() {
        UserServiceImpl service = new UserServiceImpl(new UserConverter());
        UserMapper mapper = mock(UserMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.selectList(any())).thenReturn(Collections.singletonList(new User()));

        assertEquals(1, service.selectUsers(new UserQuery()).size());
    }

    @Test
    void getByUserName_returnsNullWhenMissing() {
        UserServiceImpl service = new UserServiceImpl(new UserConverter());
        assertNull(service.getByUserName(null));
    }
}
