package com.example.demo.auth.controller;

import com.example.demo.auth.dto.*;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.*;
import com.example.demo.auth.support.AuthTokenResolver;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequireLogin;
import com.example.demo.user.entity.User;
import com.example.demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
    private final LoginAttemptService loginAttemptService;
    private final UserProfileService userProfileService;

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
                                             HttpServletRequest httpRequest,
                                             HttpServletResponse response) {
        final String credentialError = i18n("auth.login.credential.error");
        if (request == null) {
            return error(400, i18n("auth.login.request.empty"));
        }
        if (StringUtils.isBlank(request.getUserName()) || StringUtils.isBlank(request.getPassword())) {
            return error(401, credentialError);
        }
        if (loginAttemptService.isLocked(request.getUserName(), httpRequest)) {
            long remaining = loginAttemptService.getRemainingLockSeconds(request.getUserName(), httpRequest);
            return error(429, i18n("auth.login.locked", remaining));
        }
        if (StringUtils.isBlank(request.getCaptchaId()) || StringUtils.isBlank(request.getCaptchaCode())) {
            return error(400, i18n("auth.login.captcha.empty"));
        }
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            return error(401, i18n("auth.login.captcha.invalid"));
        }
        User user = userService.getByUserName(request.getUserName());
        if (user == null) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            return error(401, credentialError);
        }
        if (user.getStatus() != null && user.getStatus().equals(User.STATUS_DISABLED)) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            return error(401, credentialError);
        }
        String rawPassword = passwordService.decodeTransportPassword(request.getPassword());
        if (StringUtils.isBlank(rawPassword)) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            return error(401, credentialError);
        }
        if (!passwordService.matches(rawPassword, user.getPassword())) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            return error(401, credentialError);
        }
        loginAttemptService.clearFailures(request.getUserName(), httpRequest);
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
            return error(400, i18n("auth.logout.token.empty"));
        }
        tokenService.revoke(token);
        return success(i18n("auth.logout.success"));
    }

    /**
     * 获取当前登录用户画像信息。
     *
     * @return 用户画像信息
     */
    @GetMapping("/profile")
    @RequireLogin
    public CommonResult<UserProfileResponse> profile() {
        return success(userProfileService.buildProfile(AuthContext.get()));
    }

    /**
     * 更新当前登录用户资料（不允许修改用户名）。
     *
     * @param request 更新请求
     * @return 通用响应结果
     */
    @PutMapping("/profile")
    @RequireLogin
    public CommonResult<Void> updateProfile(@Valid @RequestBody(required = false) UserProfileUpdateRequest request) {
        if (request == null) {
            return error(400, i18n("common.request.invalid"));
        }
        AuthUser authUser = AuthContext.get();
        if (authUser == null || authUser.getId() == null) {
            return error(401, i18n("auth.user.invalid"));
        }
        User dbUser = userService.getById(authUser.getId());
        if (dbUser == null) {
            return error(404, i18n("user.not.found"));
        }
        String oldPasswordCipher = request.getOldPassword();
        String newPasswordCipher = request.getNewPassword();
        boolean wantsPasswordChange = StringUtils.isNotBlank(oldPasswordCipher) || StringUtils.isNotBlank(newPasswordCipher);
        String newRawPassword = null;
        if (wantsPasswordChange) {
            if (StringUtils.isBlank(oldPasswordCipher) || StringUtils.isBlank(newPasswordCipher)) {
                return error(400, i18n("user.password.invalid"));
            }
            String oldRawPassword = passwordService.decodeTransportPassword(oldPasswordCipher);
            newRawPassword = passwordService.decodeTransportPassword(newPasswordCipher);
            if (StringUtils.isBlank(oldRawPassword) || StringUtils.isBlank(newRawPassword)) {
                return error(400, i18n("user.password.invalid"));
            }
            if (!passwordService.matches(oldRawPassword, dbUser.getPassword())) {
                return error(400, i18n("user.password.old.invalid"));
            }
            if (newRawPassword.length() < 6) {
                return error(400, i18n("user.password.length.invalid"));
            }
            if (!passwordService.isStrongPassword(newRawPassword)) {
                return error(400, i18n("user.password.weak"));
            }
        }
        if (!userService.updateSelfProfile(authUser.getId(), request, newRawPassword)) {
            return error(500, i18n("common.update.failed"));
        }
        return success();
    }
}
