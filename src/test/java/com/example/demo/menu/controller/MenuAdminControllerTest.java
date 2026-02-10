package com.example.demo.menu.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.web.AuthTokenFilter;
import com.example.demo.common.i18n.I18nConfig;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.web.CommonExcludePathsProperties;
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
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.mapper.UserRoleMapper;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class, CommonExcludePathsProperties.class, I18nConfig.class, I18nService.class})
@TestPropertySource(properties = "security.permission.enabled=false")
class MenuAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void list_returnsMenus() throws Exception {
        Menu menu = new Menu(1L, "Dashboard", "DASH", null, "/dash", "Dashboard", "dash:view", 1, 0, "remark");
        when(menuService.list()).thenReturn(Collections.singletonList(menu));

        mockMvc.perform(get("/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Dashboard"));
    }

    @Test
    void create_savesMenu() throws Exception {
        when(menuService.getOne(any())).thenReturn(null);
        when(menuService.save(any(Menu.class))).thenReturn(true);

        mockMvc.perform(post("/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dashboard\",\"code\":\"DASH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Dashboard"));

        ArgumentCaptor<Menu> captor = ArgumentCaptor.forClass(Menu.class);
        verify(menuService).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(1);
        assertThat(captor.getValue().getSort()).isEqualTo(0);
    }

    @Test
    void updateStatus_updatesMenu() throws Exception {
        Menu menu = new Menu(1L, "Dashboard", "DASH", null, "/dash", "Dashboard", "dash:view", 1, 0, "remark");
        when(menuService.getById(1L)).thenReturn(menu);
        when(menuService.updateStatus(1L, 0)).thenReturn(true);

        mockMvc.perform(put("/menus/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":0}"))
                .andExpect(status().isOk());
    }
}
