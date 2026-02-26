package com.example.demo.auth.controller;

import com.example.demo.auth.config.AuthConstants;
import com.example.demo.auth.dto.*;
import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.auth.service.*;
import com.example.demo.auth.support.AuthTokenResolver;
import com.example.demo.auth.support.ClientIpResolver;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequireLogin;
import com.example.demo.datascope.model.RoleDataScope;
import com.example.demo.datascope.model.UserScopeOverride;
import com.example.demo.identity.api.dto.*;
import com.example.demo.identity.api.facade.IdentityCredentialApi;
import com.example.demo.identity.api.facade.IdentityProfileCommandApi;
import com.example.demo.identity.api.facade.IdentityReadFacade;
import com.example.demo.log.api.event.LoginLogEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

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
    private final PasswordPolicyService passwordPolicyService;
    private final AuthUserStatusCache authUserStatusCache;
    private final IdentityReadFacade identityReadFacade;
    private final IdentityCredentialApi identityCredentialApi;
    private final IdentityProfileCommandApi identityProfileCommandApi;
    private final LoginAttemptService loginAttemptService;
    private final LoginAnomalyAlertService loginAnomalyAlertService;
    private final OperationConfirmService operationConfirmService;
    private final UserProfileService userProfileService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthTokenResolver authTokenResolver;
    private final CaptchaRateLimitKeyResolver captchaRateLimitKeyResolver;
    private final ClientIpResolver clientIpResolver;
    private final AuthConstants systemConstants;

    /**
     * 生成验证码并返回验证码 ID 与图片数据。
     *
     * @return 包含验证码信息的通用响应
     */
    @GetMapping("/captcha")
    public CommonResult<CaptchaResponse> captcha(HttpServletRequest request) {
        String scopeKey = captchaRateLimitKeyResolver.resolve(request);
        CaptchaResponse response = captchaService.createCaptcha(scopeKey);
        if (response == null) {
            return error(systemConstants.getController().getTooManyRequestsCode(), i18n("auth.captcha.too.many"));
        }
        return success(response);
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
        AuthConstants.Controller controllerConstants = systemConstants.getController();
        int loginType = systemConstants.getLoginLog().getTypeLogin();
        int loginSuccessStatus = systemConstants.getLoginLog().getStatusSuccess();
        int loginFailStatus = systemConstants.getLoginLog().getStatusFail();
        if (request == null) {
            publishLoginLog(null, null, loginType, loginFailStatus,
                    i18n("auth.login.request.empty"), httpRequest);
            return error(controllerConstants.getBadRequestCode(), i18n("auth.login.request.empty"));
        }
        if (StringUtils.isBlank(request.getUserName()) || StringUtils.isBlank(request.getPassword())) {
            publishLoginLog(request.getUserName(), null, loginType, loginFailStatus,
                    credentialError, httpRequest);
            return error(controllerConstants.getUnauthorizedCode(), credentialError);
        }
        long remaining = loginAttemptService.getRemainingLockSeconds(request.getUserName(), httpRequest);
        if (remaining > 0) {
            publishLoginLog(request.getUserName(), null, loginType, loginFailStatus,
                    i18n("auth.login.locked", remaining), httpRequest);
            return error(controllerConstants.getTooManyRequestsCode(), i18n("auth.login.locked", remaining));
        }
        if (StringUtils.isBlank(request.getCaptchaId()) || StringUtils.isBlank(request.getCaptchaCode())) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            publishLoginLog(request.getUserName(), null, loginType, loginFailStatus,
                    i18n("auth.login.captcha.empty"), httpRequest);
            return error(controllerConstants.getBadRequestCode(), i18n("auth.login.captcha.empty"));
        }
        if (!captchaService.verify(request.getCaptchaId(), request.getCaptchaCode())) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            publishLoginLog(request.getUserName(), null, loginType, loginFailStatus,
                    i18n("auth.login.captcha.invalid"), httpRequest);
            return error(controllerConstants.getUnauthorizedCode(), i18n("auth.login.captcha.invalid"));
        }
        IdentityUserDTO user = identityReadFacade.getUserByUserName(request.getUserName());
        if (user == null) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            publishLoginLog(request.getUserName(), null, loginType, loginFailStatus,
                    credentialError, httpRequest);
            return error(controllerConstants.getUnauthorizedCode(), credentialError);
        }
        if (user.getStatus() != null && user.getStatus().equals(0)) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            publishLoginLog(request.getUserName(), user.getId(), loginType, loginFailStatus,
                    credentialError, httpRequest);
            return error(controllerConstants.getUnauthorizedCode(), credentialError);
        }
        String rawPassword = passwordService.decodeTransportPassword(request.getPassword());
        if (StringUtils.isBlank(rawPassword)) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            publishLoginLog(request.getUserName(), user.getId(), loginType, loginFailStatus,
                    credentialError, httpRequest);
            return error(controllerConstants.getUnauthorizedCode(), credentialError);
        }
        if (!identityCredentialApi.matchesPasswordById(user.getId(), rawPassword)) {
            loginAttemptService.recordFailure(request.getUserName(), httpRequest);
            publishLoginLog(request.getUserName(), user.getId(), loginType, loginFailStatus,
                    credentialError, httpRequest);
            return error(controllerConstants.getUnauthorizedCode(), credentialError);
        }
        loginAttemptService.clearFailures(request.getUserName(), httpRequest);
        AuthUser authUser = new AuthUser();
        authUser.setId(user.getId());
        authUser.setUserName(user.getUserName());
        authUser.setNickName(user.getNickName());
        authUser.setDeptId(user.getDeptId());
        if (user.getDeptId() != null) {
            String deptName = identityReadFacade.getDeptNameById(user.getDeptId());
            if (StringUtils.isNotBlank(deptName)) {
                authUser.setDeptName(deptName);
            }
        }
        authUser.setDataScopeType(user.getDataScopeType());
        authUser.setDataScopeValue(user.getDataScopeValue());
        IdentityDataScopeProfileDTO profile = identityReadFacade.buildDataScopeProfile(user.getId());
        authUser.setDeptTreeIds(profile.getDeptTreeIds());
        authUser.setRoleDataScopes(mapRoleDataScopes(profile.getRoleDataScopes()));
        authUser.setUserScopeOverrides(mapUserScopeOverrides(profile.getUserScopeOverrides()));
        boolean firstLoginForceChange = passwordPolicyService.isFirstLoginForceChange(user);
        boolean passwordExpired = passwordPolicyService.isPasswordExpired(user);
        LoginResponse loginResponse = tokenService.issueToken(authUser);
        loginResponse.setFirstLoginForceChange(firstLoginForceChange);
        loginResponse.setPasswordExpired(passwordExpired);
        loginResponse.setPasswordChangeRequired(firstLoginForceChange || passwordExpired);
        loginResponse.setPasswordExpireDays(passwordPolicyService.getExpireDays());
        AuthConstants.Token tokenConstants = systemConstants.getToken();
        response.setHeader(tokenConstants.getAuthorizationHeader(), tokenConstants.getBearerPrefix() + loginResponse.getToken());
        String loginIp = resolveClientIp(httpRequest);
        String loginUserAgent = httpRequest == null ? null : httpRequest.getHeader(systemConstants.getProfile().getUserAgentHeader());
        loginAnomalyAlertService.checkAndNotify(user, loginIp, loginUserAgent, LocalDateTime.now());
        publishLoginLog(user.getUserName(), user.getId(), loginType, loginSuccessStatus,
                i18n("auth.login.success"), httpRequest);
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
                                     HttpServletResponse response,
                                     @RequestBody(required = false) LogoutRequest body) {
        String token = authTokenResolver.resolve(request);
        if (StringUtils.isBlank(token) && body != null) {
            token = body.getToken();
        }
        if (StringUtils.isBlank(token)) {
            return error(systemConstants.getController().getBadRequestCode(), i18n("auth.logout.token.empty"));
        }
        AuthUser loginUser = tokenService.verifyToken(token);
        tokenService.revoke(loginUser == null ? null : loginUser.getId(), token);
        int logoutType = systemConstants.getLoginLog().getTypeLogout();
        int successStatus = systemConstants.getLoginLog().getStatusSuccess();
        publishLoginLog(loginUser == null ? null : loginUser.getUserName(),
                loginUser == null ? null : loginUser.getId(),
                logoutType,
                successStatus,
                i18n("auth.logout.success"),
                request);
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
        AuthConstants.Controller controllerConstants = systemConstants.getController();
        if (request == null) {
            return error(controllerConstants.getBadRequestCode(), i18n("common.request.invalid"));
        }
        AuthUser authUser = AuthContext.get();
        if (authUser == null || authUser.getId() == null) {
            return error(controllerConstants.getUnauthorizedCode(), i18n("auth.user.invalid"));
        }
        IdentityUserDTO dbUser = identityReadFacade.getUserById(authUser.getId());
        if (dbUser == null) {
            return error(controllerConstants.getNotFoundCode(), i18n("user.not.found"));
        }
        String oldPasswordCipher = request.getOldPassword();
        String newPasswordCipher = request.getNewPassword();
        boolean wantsPasswordChange = StringUtils.isNotBlank(oldPasswordCipher) || StringUtils.isNotBlank(newPasswordCipher);
        String newRawPassword = null;
        if (wantsPasswordChange) {
            if (StringUtils.isBlank(oldPasswordCipher) || StringUtils.isBlank(newPasswordCipher)) {
                return error(controllerConstants.getBadRequestCode(), i18n("user.password.invalid"));
            }
            String oldRawPassword = passwordService.decodeTransportPassword(oldPasswordCipher);
            newRawPassword = passwordService.decodeTransportPassword(newPasswordCipher);
            if (StringUtils.isBlank(oldRawPassword) || StringUtils.isBlank(newRawPassword)) {
                return error(controllerConstants.getBadRequestCode(), i18n("user.password.invalid"));
            }
            if (!identityCredentialApi.matchesPasswordById(authUser.getId(), oldRawPassword)) {
                return error(controllerConstants.getBadRequestCode(), i18n("user.password.old.invalid"));
            }
            if (newRawPassword.length() < systemConstants.getProfile().getNewPasswordMinLength()) {
                return error(controllerConstants.getBadRequestCode(), i18n("user.password.length.invalid"));
            }
            if (!passwordService.isStrongPassword(newRawPassword)) {
                return error(controllerConstants.getBadRequestCode(), i18n("user.password.weak"));
            }
        }
        IdentityUserProfileUpdateRequest profileUpdate = new IdentityUserProfileUpdateRequest();
        profileUpdate.setNickName(request.getNickName());
        profileUpdate.setPhone(request.getPhone());
        profileUpdate.setEmail(request.getEmail());
        profileUpdate.setSex(request.getSex());
        profileUpdate.setRemark(request.getRemark());
        if (!identityProfileCommandApi.updateSelfProfile(authUser.getId(), profileUpdate, newRawPassword)) {
            return error(controllerConstants.getInternalServerErrorCode(), i18n("common.update.failed"));
        }
        authUserStatusCache.invalidate(authUser.getId());
        if (wantsPasswordChange) {
            tokenService.revokeByUserId(authUser.getId());
        }
        return success();
    }

    /**
     * 发送敏感操作邮箱验证码。
     *
     * @param request 请求参数
     * @return 通用响应
     */
    @PostMapping("/security/operation-confirm/send")
    @RequireLogin
    public CommonResult<Void> sendOperationConfirmCode(@Valid @RequestBody OperationConfirmSendRequest request) {
        OperationConfirmService.SendCodeResult result = operationConfirmService.sendCode(
                AuthContext.get(),
                request == null ? null : request.getActionKey(),
                request == null ? null : request.getActionLabel()
        );
        if (!result.isSuccess()) {
            if ("auth.operation.confirm.send.too.frequent".equals(result.getMessageKey())) {
                return error(result.getCode(), i18n(result.getMessageKey(), result.getRetryAfterSeconds()));
            }
            return error(result.getCode(), i18n(result.getMessageKey()));
        }
        return success(i18n(result.getMessageKey()));
    }

    /**
     * 校验敏感操作邮箱验证码，成功后返回短期票据。
     *
     * @param request 请求参数
     * @return 校验结果与票据
     */
    @PostMapping("/security/operation-confirm/verify")
    @RequireLogin
    public CommonResult<OperationConfirmVerifyResponse> verifyOperationConfirmCode(
            @Valid @RequestBody OperationConfirmVerifyRequest request) {
        OperationConfirmService.VerifyCodeResult result = operationConfirmService.verifyCode(
                AuthContext.get(),
                request == null ? null : request.getActionKey(),
                request == null ? null : request.getCode()
        );
        if (!result.isSuccess()) {
            return error(result.getCode(), i18n(result.getMessageKey()));
        }
        OperationConfirmVerifyResponse response = new OperationConfirmVerifyResponse();
        response.setTicket(result.getTicket());
        response.setExpiresAt(result.getExpiresAt());
        return success(i18n(result.getMessageKey()), response);
    }

    private void publishLoginLog(String userName,
                                 Long userId,
                                 int loginType,
                                 int status,
                                 String message,
                                 HttpServletRequest request) {
        if (eventPublisher == null) {
            return;
        }
        String ip = resolveClientIp(request);
        String ua = request == null ? null : request.getHeader(systemConstants.getProfile().getUserAgentHeader());
        eventPublisher.publishEvent(new LoginLogEvent(
                userName,
                userId,
                loginType,
                status,
                message,
                ip,
                ua,
                LocalDateTime.now()
        ));
    }

    private String resolveClientIp(HttpServletRequest request) {
        return clientIpResolver.resolve(request);
    }

    private List<RoleDataScope> mapRoleDataScopes(List<IdentityRoleDataScopeDTO> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyList();
        }
        List<RoleDataScope> result = new ArrayList<>(sources.size());
        for (IdentityRoleDataScopeDTO source : sources) {
            if (source == null) {
                continue;
            }
            RoleDataScope scope = new RoleDataScope();
            scope.setRoleId(source.getRoleId());
            scope.setRoleCode(source.getRoleCode());
            scope.setDataScopeType(source.getDataScopeType());
            if (source.getCustomDeptIds() != null) {
                scope.setCustomDeptIds(new LinkedHashSet<>(source.getCustomDeptIds()));
            }
            if (source.getMenuDataScopes() != null) {
                scope.setMenuDataScopes(new LinkedHashMap<>(source.getMenuDataScopes()));
            }
            if (source.getMenuCustomDepts() != null) {
                LinkedHashMap<String, Set<Long>> menuCustoms = new LinkedHashMap<>();
                for (Map.Entry<String, Set<Long>> entry : source.getMenuCustomDepts().entrySet()) {
                    menuCustoms.put(entry.getKey(),
                            entry.getValue() == null ? new LinkedHashSet<>() : new LinkedHashSet<>(entry.getValue()));
                }
                scope.setMenuCustomDepts(menuCustoms);
            }
            result.add(scope);
        }
        return result;
    }

    private Map<String, UserScopeOverride> mapUserScopeOverrides(Map<String, IdentityUserScopeOverrideDTO> sources) {
        if (sources == null || sources.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, UserScopeOverride> result = new LinkedHashMap<>();
        for (Map.Entry<String, IdentityUserScopeOverrideDTO> entry : sources.entrySet()) {
            IdentityUserScopeOverrideDTO source = entry.getValue();
            if (source == null) {
                continue;
            }
            UserScopeOverride override = new UserScopeOverride();
            override.setScopeKey(source.getScopeKey());
            override.setDataScopeType(source.getDataScopeType());
            if (source.getCustomDeptIds() != null) {
                override.setCustomDeptIds(new LinkedHashSet<>(source.getCustomDeptIds()));
            }
            override.setStatus(source.getStatus());
            result.put(entry.getKey(), override);
        }
        return result;
    }

}
