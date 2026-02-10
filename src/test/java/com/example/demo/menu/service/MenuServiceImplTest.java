package com.example.demo.menu.service;

import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.mapper.MenuMapper;
import com.example.demo.menu.service.impl.MenuServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuServiceImplTest {

    @Test
    void updateStatus_updatesById() {
        MenuServiceImpl service = new MenuServiceImpl();
        MenuMapper mapper = mock(MenuMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.updateById(any(Menu.class))).thenReturn(1);

        assertTrue(service.updateStatus(1L, 0));
    }

    @Test
    void updateStatus_returnsFalseWhenIdMissing() {
        MenuServiceImpl service = new MenuServiceImpl();
        assertFalse(service.updateStatus(null, 1));
    }
}
