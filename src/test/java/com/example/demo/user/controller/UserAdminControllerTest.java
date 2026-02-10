package com.example.demo.user.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.service.PasswordService;
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
import com.example.demo.dept.service.DeptService;
import com.example.demo.menu.mapper.MenuMapper;
import com.example.demo.menu.mapper.RoleMenuMapper;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.mapper.UserRoleMapper;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserCreateRequest;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class,
        CommonExcludePathsProperties.class, I18nConfig.class, I18nService.class})
@TestPropertySource(properties = "security.permission.enabled=false")
class UserAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserConverter userConverter;

    @MockBean
    private DeptService deptService;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private TokenService tokenService;

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
    private RolePermissionMapper rolePermissionMapper;

    @MockBean
    private UserRoleMapper userRoleMapper;

    @MockBean
    private DataScopeRuleMapper dataScopeRuleMapper;

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
    void create_rejectsExistingUsername() throws Exception {
        when(userService.getByUserName("alice")).thenReturn(new User());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "zh-CN")
                        .content("{\"userName\":\"alice\",\"password\":\"Abc123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("\u7528\u6237\u540d\u5df2\u5b58\u5728"));
    }

    @Test
    void create_success_returnsUserView() throws Exception {
        when(userService.getByUserName("alice")).thenReturn(null);
        when(deptService.getById(1L)).thenReturn(new com.example.demo.dept.entity.Dept());
        when(passwordService.resolveRawPassword("Abc123!")).thenReturn("Abc123!");
        when(passwordService.isStrongPassword("Abc123!")).thenReturn(true);
        User created = new User();
        created.setId(1L);
        created.setUserName("alice");
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(created);
        UserVO view = new UserVO();
        view.setId(1L);
        view.setUserName("alice");
        when(userConverter.toView(any(User.class), anyList())).thenReturn(view);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "zh-CN")
                        .content("{\"userName\":\"alice\",\"password\":\"Abc123!\",\"deptId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userName").value("alice"));
    }

    @Test
    void update_rejectsMissingUser() throws Exception {
        when(userService.getById(1L)).thenReturn(null);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "zh-CN")
                        .content("{\"userName\":\"alice\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("\u7528\u6237\u4e0d\u5b58\u5728"));
    }

    @Test
    void updateStatus_rejectsInvalidStatus() throws Exception {
        when(userService.getById(1L)).thenReturn(new User());

        mockMvc.perform(put("/users/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "zh-CN")
                        .content("{\"status\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("\u72b6\u6001\u4e0d\u5408\u6cd5"));
    }

    @Test
    void resetPassword_rejectsInvalidPassword() throws Exception {
        when(userService.getById(1L)).thenReturn(new User());
        when(passwordService.decodeTransportPassword("badpwd")).thenReturn(null);

        mockMvc.perform(put("/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "zh-CN")
                        .content("{\"newPassword\":\"badpwd\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("\u5bc6\u7801\u65e0\u6548"));
    }

    @Test
    void assignRoles_success() throws Exception {
        when(userService.getById(1L)).thenReturn(new User());
        when(userService.assignRoles(anyLong(), anyList())).thenReturn(true);

        mockMvc.perform(put("/users/1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleIds\":[1,2]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).assignRoles(anyLong(), anyList());
    }

    @Test
    void updateDataScope_returnsFailure() throws Exception {
        when(userService.getById(1L)).thenReturn(new User());
        when(userService.updateDataScope(anyLong(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(put("/users/1/data-scope")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Accept-Language", "zh-CN")
                        .content("{\"dataScopeType\":\"CUSTOM\",\"dataScopeValue\":\"1,2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("\u66f4\u65b0\u6570\u636e\u8303\u56f4\u5931\u8d25"));

        verify(userService).updateDataScope(1L, "CUSTOM", "1,2");
    }
}
