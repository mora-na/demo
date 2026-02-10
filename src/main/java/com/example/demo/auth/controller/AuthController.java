package com.example.demo.auth.controller;

import com.example.demo.auth.dto.CaptchaResponse;
import com.example.demo.auth.dto.LoginRequest;
import com.example.demo.auth.dto.LoginResponse;
import com.example.demo.auth.dto.LogoutRequest;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.CaptchaService;
import com.example.demo.auth.service.PasswordService;
import com.example.demo.auth.service.TokenService;
import com.example.demo.auth.support.AuthTokenResolver;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证接口控制器，处理验证码、登录与登出流程。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/9
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final CaptchaService captchaService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final UserService userService;

    /**
     * 生成验证码并返回验证码 ID 与图片数据。
     *
     * @return 包含验证码信息的通用响应
     */
    @GetMapping("/captcha")
    public CommonResult<CaptchaResponse> captcha() {
        return success(captchaService.createCaptcha());
    }

    /**
     * 登录接口，校验账号、密码与验证码，签发 JWT 并写入响应头。
     *
     * @param request  登录请求参数
     * @param response HTTP 响应
     * @return 登录结果与令牌信息
     */
    @PostMapping("/login")
    public CommonResult<LoginResponse> login(@RequestBody(required = false) LoginRequest request,
                                             HttpServletResponse response) {
        if (request == null) {
            return error(400, "request body is empty");
        }
        if (StringUtils.isBlank(request.getUserName()) || StringUtils.isBlank(request.getPassword())) {
            return error(400, "username or password is empty");
        }
        if (StringUtils.isBlank(request.getCaptchaId()) || StringUtils.isBlank(request.getCaptchaCode())) {
            return error(400, "captcha is empty");
        }
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return error(401, "captcha is invalid");
        }
        User user = userService.getByUserName(request.getUserName());
        if (user == null) {
            return error(401, "user not found");
        }
        if (user.getStatus() != null && user.getStatus().equals(User.STATUS_DISABLED)) {
            return error(403, "user is disabled");
        }
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            return error(401, "password is incorrect");
        }
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setUserName(user.getUserName());
        authUser.setNickName(user.getNickName());
        authUser.setDeptId(user.getDeptId());
        authUser.setDataScopeType(user.getDataScopeType());
        authUser.setDataScopeValue(user.getDataScopeValue());
        LoginResponse loginResponse = tokenService.issueToken(authUser);
        response.setHeader("Authorization", "Bearer " + loginResponse.getToken());
        return success(loginResponse);
    }

    /**
     * 登出接口，撤销当前令牌。
     *
     * @param request HTTP 请求
     * @param body    登出请求体（可选）
     * @return 通用响应结果
     */
    @PostMapping("/logout")
    public CommonResult<Void> logout(HttpServletRequest request,
                                     @RequestBody(required = false) LogoutRequest body) {
        String token = AuthTokenResolver.resolve(request);
        if (StringUtils.isBlank(token) && body != null) {
            token = body.getToken();
        }
        if (StringUtils.isBlank(token)) {
            return error(400, "token is empty");
        }
        tokenService.revoke(token);
        return success("logout success");
    }
}
