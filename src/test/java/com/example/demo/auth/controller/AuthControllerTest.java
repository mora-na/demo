package com.example.demo.auth.controller;

import com.example.demo.auth.config.AuthProperties;
import com.example.demo.auth.dto.CaptchaResponse;
import com.example.demo.auth.dto.LoginResponse;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.CaptchaService;
import com.example.demo.auth.service.PasswordService;
import com.example.demo.auth.service.TokenService;
import com.example.demo.order.mapper.OrderMapper;
import com.example.demo.user.entity.User;
import com.example.demo.user.mapper.UserMapper;
import com.example.demo.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
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
    private UserMapper userMapper;

    @MockBean
    private OrderMapper orderMapper;

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
        User user = new User(1L, "alice", "Ali", "pw", "F", "note");
        when(userService.getByUserName("alice")).thenReturn(user);
        when(passwordService.matches("pw", "pw")).thenReturn(true);
        when(tokenService.issueToken(any(AuthUser.class)))
                .thenReturn(new LoginResponse("token", "Bearer", 100L, new AuthUser(1L, "alice", "Ali")));

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
