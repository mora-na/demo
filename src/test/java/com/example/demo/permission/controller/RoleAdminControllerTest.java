package com.example.demo.permission.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.service.TokenService;
import com.example.demo.common.web.limit.DuplicateSubmitProperties;
import com.example.demo.common.web.limit.RateLimitProperties;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.common.web.xss.XssProperties;
import com.example.demo.datascope.mapper.DataScopeRuleMapper;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.entity.Role;
import com.example.demo.permission.entity.RolePermission;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.mapper.UserRoleMapper;
import com.example.demo.permission.service.PermissionService;
import com.example.demo.permission.service.RolePermissionService;
import com.example.demo.permission.service.RoleService;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class})
@TestPropertySource(properties = "security.permission.enabled=false")
class RoleAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private RolePermissionService rolePermissionService;

    @MockBean
    private PermissionService permissionService;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private PermissionMapper permissionMapper;

    @MockBean
    private RoleMapper roleMapper;

    @MockBean
    private UserRoleMapper userRoleMapper;

    @MockBean
    private RolePermissionMapper rolePermissionMapper;

    @MockBean
    private DataScopeRuleMapper dataScopeRuleMapper;

    @MockBean(name = "permissionServiceForInterceptor")
    private com.example.demo.common.web.permission.PermissionService permissionServiceForInterceptor;

    @Test
    void list_returnsRolesWithPermissions() throws Exception {
        Role role = new Role(1L, "admin", "Administrator", 1);
        when(roleService.list()).thenReturn(Collections.singletonList(role));
        RolePermission rp = new RolePermission(10L, 1L, 2L);
        when(rolePermissionService.list()).thenReturn(Collections.singletonList(rp));

        mockMvc.perform(get("/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("admin"))
                .andExpect(jsonPath("$.data[0].permissionIds[0]").value(2));
    }

    @Test
    void create_savesRole() throws Exception {
        when(roleService.getOne(any())).thenReturn(null);
        when(roleService.save(any(Role.class))).thenReturn(true);

        mockMvc.perform(post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"editor\",\"name\":\"Editor\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("editor"));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(roleService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
    }

    @Test
    void assignPermissions_checksExistenceAndSaves() throws Exception {
        Role role = new Role(1L, "admin", "Administrator", 1);
        when(roleService.getById(1L)).thenReturn(role);
        Permission p1 = new Permission(2L, "sys:user:list", "List", 1);
        Permission p2 = new Permission(3L, "sys:user:create", "Create", 1);
        when(permissionService.listByIds(Arrays.asList(2L, 3L))).thenReturn(Arrays.asList(p1, p2));
        when(roleService.assignPermissions(anyLong(), any())).thenReturn(true);

        mockMvc.perform(put("/roles/1/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"permissionIds\":[2,3]}"))
                .andExpect(status().isOk());

        verify(roleService).assignPermissions(1L, Arrays.asList(2L, 3L));
    }
}
