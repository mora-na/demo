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
import com.example.demo.dept.mapper.DeptMapper;
import com.example.demo.menu.entity.Menu;
import com.example.demo.menu.mapper.MenuMapper;
import com.example.demo.menu.mapper.RoleMenuMapper;
import com.example.demo.menu.service.MenuService;
import com.example.demo.menu.service.RoleMenuService;
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
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class, CommonExcludePathsProperties.class, FastJsonWebMvcConfig.class})
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
    private RoleMenuService roleMenuService;

    @MockBean
    private MenuService menuService;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private DeptMapper deptMapper;

    @MockBean
    private MenuMapper menuMapper;

    @MockBean
    private RoleMenuMapper roleMenuMapper;

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
    private AuthTokenFilter authTokenFilter;

    @MockBean
    private DuplicateSubmitFilter duplicateSubmitFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private XssFilter xssFilter;

    @MockBean
    private PermissionInterceptor permissionInterceptor;

    @MockBean
    private DataScopeRuleMapper dataScopeRuleMapper;

    @MockBean(name = "permissionServiceForInterceptor")
    private com.example.demo.common.web.permission.PermissionService permissionServiceForInterceptor;

    @BeforeEach
    void setupPermissionInterceptor() throws Exception {
        when(permissionInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void list_returnsRolesWithPermissions() throws Exception {
        Role role = new Role(1L, "admin", "Administrator", 1, null, null);
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
        Role role = new Role(1L, "admin", "Administrator", 1, null, null);
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

    @Test
    void assignMenus_checksExistenceAndSaves() throws Exception {
        Role role = new Role(1L, "admin", "Administrator", 1, null, null);
        when(roleService.getById(1L)).thenReturn(role);
        Menu m1 = new Menu(2L, "Dashboard", "DASH", null, "/dash", "Dashboard", "dash:view", 1, 0, null);
        Menu m2 = new Menu(3L, "Users", "USER", null, "/users", "Users", "user:query", 1, 0, null);
        when(menuService.listByIds(Arrays.asList(2L, 3L))).thenReturn(Arrays.asList(m1, m2));
        when(roleMenuService.assignMenus(anyLong(), any())).thenReturn(true);

        mockMvc.perform(put("/roles/1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[2,3]}"))
                .andExpect(status().isOk());

        verify(roleMenuService).assignMenus(1L, Arrays.asList(2L, 3L));
    }
}
