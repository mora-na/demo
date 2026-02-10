package com.example.demo.permission.service;

import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PermissionServiceImplTest {

    @Test
    void updateStatus_returnsFalseWhenIdMissing() {
        PermissionServiceImpl service = new PermissionServiceImpl();
        assertFalse(service.updateStatus(null, 1));
    }

    @Test
    void updateStatus_updatesById() {
        PermissionServiceImpl service = new PermissionServiceImpl();
        PermissionMapper mapper = mock(PermissionMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.updateById(any(Permission.class))).thenReturn(1);

        assertTrue(service.updateStatus(1L, 1));
    }
}
