package com.example.demo.auth.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.CaptchaResponse;
import com.example.demo.auth.dto.LoginResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.CaptchaService;
import com.example.demo.auth.service.PasswordService;
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
import com.example.demo.common.web.permission.PermissionService;
import com.example.demo.common.web.xss.XssProperties;
import com.example.demo.datascope.mapper.DataScopeRuleMapper;
import com.example.demo.dept.mapper.DeptMapper;
import com.example.demo.menu.mapper.MenuMapper;
import com.example.demo.menu.mapper.RoleMenuMapper;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.mapper.UserRoleMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class, CommonExcludePathsProperties.class, FastJsonWebMvcConfig.class})
@TestPropertySource(properties = "security.permission.enabled=false")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaptchaService captchaService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private PasswordService passwordService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private PermissionService permissionService;

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
    void captcha_returnsPayload() throws Exception {
        when(captchaService.createCaptcha()).thenReturn(new CaptchaResponse("cid", "data:image/png;base64,abc", 120));

        mockMvc.perform(get("/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.captchaId").value("cid"));
    }

    @Test
    void login_success_setsAuthorizationHeader() throws Exception {
        when(captchaService.verify("cid", "code")).thenReturn(true);
        User user = new User();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Ali");
        user.setPassword("pw");
        user.setSex("F");
        user.setTst("note");
        user.setStatus(1);
        when(userService.getByUserName("alice")).thenReturn(user);
        when(passwordService.matches("pw", "pw")).thenReturn(true);
        AuthUser authUser = new AuthUser();
        authUser.setId(1L);
        authUser.setUserName("alice");
        authUser.setNickName("Ali");
        when(tokenService.issueToken(any(AuthUser.class)))
                .thenReturn(new LoginResponse("token", "Bearer", 100L, authUser));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"alice\",\"password\":\"pw\",\"captchaId\":\"cid\",\"captchaCode\":\"code\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer token"))
                .andExpect(jsonPath("$.data.token").value("token"));
    }

    @Test
    void login_rejectsMissingBody() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("request body is empty"));
    }

    @Test
    void login_rejectsInvalidCaptcha() throws Exception {
        when(captchaService.verify("cid", "code")).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"alice\",\"password\":\"pw\",\"captchaId\":\"cid\",\"captchaCode\":\"code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("captcha is invalid"));
    }

    @Test
    void logout_usesHeaderToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("logout success"));

        verify(tokenService).revoke("token");
    }

    @Test
    void logout_rejectsMissingToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("token is empty"));
    }
}
