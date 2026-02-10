package com.example.demo.permission.service;

import com.example.demo.permission.entity.Role;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    @Test
    void assignPermissions_returnsFalseWhenRoleIdMissing() {
        RolePermissionService rolePermissionService = mock(RolePermissionService.class);
        RoleServiceImpl service = new RoleServiceImpl(rolePermissionService);

        assertFalse(service.assignPermissions(null, Collections.singletonList(1L)));
    }

    @Test
    void assignPermissions_returnsTrueWhenEmptyPermissions() {
        RolePermissionService rolePermissionService = mock(RolePermissionService.class);
        when(rolePermissionService.remove(any())).thenReturn(true);
        RoleServiceImpl service = new RoleServiceImpl(rolePermissionService);

        assertTrue(service.assignPermissions(1L, Collections.emptyList()));
        verify(rolePermissionService).remove(any());
    }

    @Test
    void assignPermissions_savesDistinctRelations() {
        RolePermissionService rolePermissionService = mock(RolePermissionService.class);
        when(rolePermissionService.remove(any())).thenReturn(true);
        when(rolePermissionService.saveBatch(anyList())).thenReturn(true);
        RoleServiceImpl service = new RoleServiceImpl(rolePermissionService);

        List<Long> permissionIds = Arrays.asList(1L, 1L, null, 2L);
        assertTrue(service.assignPermissions(1L, permissionIds));

        verify(rolePermissionService).saveBatch(anyList());
    }

    @Test
    void updateStatus_updatesById() {
        RolePermissionService rolePermissionService = mock(RolePermissionService.class);
        RoleServiceImpl service = new RoleServiceImpl(rolePermissionService);
        RoleMapper mapper = mock(RoleMapper.class);
        ReflectionTestUtils.setField(service, "baseMapper", mapper);

        when(mapper.updateById(any(Role.class))).thenReturn(1);

        assertTrue(service.updateStatus(1L, 1));
    }
}
