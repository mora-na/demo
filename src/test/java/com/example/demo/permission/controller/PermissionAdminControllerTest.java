package com.example.demo.permission.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.web.AuthTokenFilter;
import com.example.demo.common.web.CommonExcludePathsProperties;
import com.example.demo.common.web.FastJsonWebMvcConfig;
import com.example.demo.common.web.filter.DuplicateSubmitFilter;
import com.example.demo.common.web.filter.RateLimitFilter;
import com.example.demo.common.web.filter.XssFilter;
import com.example.demo.common.web.limit.DuplicateSubmitProperties;
import com.example.demo.common.web.limit.RateLimitProperties;
import com.example.demo.common.web.permission.PermissionInterceptor;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.common.web.xss.XssProperties;
import com.example.demo.datascope.mapper.DataScopeRuleMapper;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.permission.entity.Permission;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.mapper.UserRoleMapper;
import com.example.demo.permission.service.PermissionService;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PermissionAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class, CommonExcludePathsProperties.class, FastJsonWebMvcConfig.class})
@TestPropertySource(properties = "security.permission.enabled=false")
class PermissionAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @MockBean
    private DuplicateSubmitFilter duplicateSubmitFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private XssFilter xssFilter;

    @MockBean
    private PermissionInterceptor permissionInterceptor;

    @BeforeEach
    void setupPermissionInterceptor() throws Exception {
        when(permissionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void list_returnsPermissions() throws Exception {
        Permission p = new Permission(1L, "sys:user:list", "List users", 1);
        when(permissionService.list()).thenReturn(Collections.singletonList(p));

        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("sys:user:list"));
    }

    @Test
    void create_savesPermission() throws Exception {
        when(permissionService.getOne(any())).thenReturn(null);
        when(permissionService.save(any(Permission.class))).thenReturn(true);

        mockMvc.perform(post("/permissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"sys:user:create\",\"name\":\"Create user\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("sys:user:create"));

        ArgumentCaptor<Permission> captor = ArgumentCaptor.forClass(Permission.class);
        verify(permissionService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
    }

    @Test
    void updateStatus_changesStatus() throws Exception {
        Permission existing = new Permission(1L, "sys:user:list", "List users", 1);
        when(permissionService.getById(1L)).thenReturn(existing);
        when(permissionService.updateStatus(anyLong(), any())).thenReturn(true);

        mockMvc.perform(put("/permissions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());

        verify(permissionService).updateStatus(1L, 0);
    }
}
