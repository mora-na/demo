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

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final CaptchaService captchaService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final UserService userService;

    @GetMapping("/captcha")
    public CommonResult<CaptchaResponse> captcha() {
        return success(captchaService.createCaptcha());
    }

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
        if (!passwordService.matches(request.getPassword(), user.getPassword())) {
            return error(401, "password is incorrect");
        }
        LoginResponse loginResponse = tokenService.issueToken(new AuthUser(user.getId(), user.getUserName(), user.getNickName()));
        response.setHeader("Authorization", "Bearer " + loginResponse.getToken());
        return success(loginResponse);
    }

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
