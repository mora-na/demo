package com.example.demo.user.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.service.TokenService;
import com.example.demo.common.tool.ExcelTool;
import com.example.demo.order.dto.OrderVO;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.user.config.UserConfig;
import com.example.demo.user.converter.UserConverter;
import com.example.demo.user.dto.UserQuery;
import com.example.demo.user.dto.UserVO;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import com.example.demo.user.service.UserViewService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
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
    private UserConfig userConfig;

    @MockBean
    private AuthProperties authProperties;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private OrderMapper orderMapper;

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
    void getKeyValue_returnsConfigValue() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("theme", "light");
        when(userConfig.getConfig()).thenReturn(config);

        mockMvc.perform(get("/hello/getKeyValue").param("key", "theme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("light"));
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
        UserVO view = new UserVO();
        view.setId(1L);
        view.setUserName("alice");
        view.setNickName("Ali");
        view.setSex("F");
        view.setTst("note");
        view.setOrderVOS(Collections.<OrderVO>emptyList());
        ByteArrayOutputStream outputStream = ExcelTool.exportToStream(Collections.singletonList(view), UserVO.class);
        MockMultipartFile file = new MockMultipartFile("file", "users.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", outputStream.toByteArray());

        when(userConverter.toEntityList(anyList())).thenReturn(Collections.singletonList(new User()));
        when(userService.saveOrUpdateBatchByMultiField(anyList())).thenReturn(true);

        mockMvc.perform(multipart("/hello/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("\u5bfc\u5165\u6210\u529f\uff01"));
    }

    @Test
    void getKeyValue_rejectsBlankKey() throws Exception {
        mockMvc.perform(get("/hello/getKeyValue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("key is empty"));
    }
}
