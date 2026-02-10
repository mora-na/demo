package com.example.demo.menu.service;

import com.example.demo.menu.mapper.RoleMenuMapper;
import com.example.demo.menu.service.impl.RoleMenuServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleMenuServiceImplTest {

    @Test
    void assignMenus_returnsFalseWhenRoleIdMissing() {
        RoleMenuServiceImpl service = new RoleMenuServiceImpl();
        assertFalse(service.assignMenus(null, Collections.singletonList(1L)));
    }

    @Test
    void assignMenus_returnsTrueWhenEmptyMenus() {
        RoleMenuServiceImpl service = new RoleMenuServiceImpl();
        RoleMenuMapper mapper = mock(RoleMenuMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.delete(any())).thenReturn(1);

        assertTrue(service.assignMenus(1L, Collections.emptyList()));
    }
}
