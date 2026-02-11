package com.example.demo.user.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.web.AuthTokenFilter;
import com.example.demo.common.i18n.I18nConfig;
import com.example.demo.common.i18n.I18nService;
import com.example.demo.common.tool.ExcelTool;
import com.example.demo.common.web.CommonExcludePathsProperties;
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
import com.example.demo.order.dto.OrderVO;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.permission.mapper.PermissionMapper;
import com.example.demo.permission.mapper.RoleMapper;
import com.example.demo.permission.mapper.RolePermissionMapper;
import com.example.demo.permission.mapper.UserRoleMapper;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserViewService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({DuplicateSubmitProperties.class, RateLimitProperties.class, PermissionProperties.class, XssProperties.class, CommonExcludePathsProperties.class, I18nConfig.class, I18nService.class})
@TestPropertySource(properties = "security.permission.enabled=false")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserViewService userViewService;

    @MockBean
    private UserConverter userConverter;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private PermissionService permissionService;

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
    void selectUsers_endpointsReturnPage() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Ali");
        user.setSex("F");
        user.setTst("note");
        UserVO view = new UserVO();
        view.setId(1L);
        view.setUserName("alice");
        view.setNickName("Ali");
        view.setSex("F");
        view.setTst("note");
        view.setOrderVOS(Collections.<OrderVO>emptyList());
        when(userService.selectUsers(nullable(UserQuery.class))).thenAnswer(invocation -> {
            Page<User> page = PageHelper.getLocalPage();
            if (page != null) {
                page.add(user);
                page.setTotal(1);
            }
            return Collections.singletonList(user);
        });
        when(userViewService.toView(any(User.class))).thenReturn(view);
        when(userViewService.toViewList(anyList())).thenReturn(Collections.singletonList(view));

        when(userService.getBaseMapper()).thenReturn(userMapper);
        when(userMapper.selectList(any())).thenAnswer(invocation -> {
            Page<User> page = PageHelper.getLocalPage();
            if (page != null) {
                page.add(user);
                page.setTotal(1);
            }
            return Collections.singletonList(user);
        });

        mockMvc.perform(get("/hello/selectUsers1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userName").value("alice"));

        mockMvc.perform(get("/hello/selectUsers2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userName").value("alice"));

        mockMvc.perform(get("/hello/selectUsers3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userName").value("alice"));

        mockMvc.perform(get("/hello/selectUsers4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userName").value("alice"));

        mockMvc.perform(get("/hello/selectUsers5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userName").value("alice"));
    }

    @Test
    void export_writesExcelResponse() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Ali");
        user.setSex("F");
        user.setTst("note");
        UserVO view = new UserVO();
        view.setId(1L);
        view.setUserName("alice");
        view.setNickName("Ali");
        view.setSex("F");
        view.setTst("note");
        view.setOrderVOS(Collections.<OrderVO>emptyList());
        when(userService.selectUsers(any(UserQuery.class))).thenReturn(Collections.singletonList(user));
        when(userViewService.toViewList(anyList())).thenReturn(Collections.singletonList(view));

        mockMvc.perform(get("/hello/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString(".xlsx")));
    }

    @Test
    void importExcel_returnsSuccess() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUserName("alice");
        user.setNickName("Ali");
        user.setSex("1");
        user.setTst("note");
        ByteArrayOutputStream outputStream = ExcelTool.exportToStream(Collections.singletonList(user), User.class);
        MockMultipartFile file = new MockMultipartFile("file", "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outputStream.toByteArray());

        when(userConverter.toEntityList(anyList())).thenReturn(Collections.singletonList(new User()));
        when(userService.saveOrUpdateBatch(anyList())).thenReturn(true);

        mockMvc.perform(multipart("/hello/import").file(file)
                        .header("Accept-Language", "zh-CN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("\u5bfc\u5165\u6210\u529f"));
    }

    @Test
    void getKeyValue_rejectsBlankKey() throws Exception {
        mockMvc.perform(get("/hello/getKeyValue")
                        .header("Accept-Language", "zh-CN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("\u914d\u7f6e\u9879\u952e\u4e3a\u7a7a"));
    }
}
