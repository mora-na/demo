package com.example.demo.log.controller;

import com.example.demo.auth.model.AuthContext;
import com.example.demo.auth.model.AuthUser;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.PermissionProperties;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.dto.OperLogQuery;
import com.example.demo.log.entity.SysOperLog;
import com.example.demo.log.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

/**
 * 操作日志管理接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@RestController
@RequestMapping("/logs/oper")
@RequiredArgsConstructor
public class OperLogController extends BaseController {

    private final SysOperLogService operLogService;
    private final LogConstants logConstants;
    private final PermissionProperties permissionProperties;

    @GetMapping
    @RequirePermission("log:query")
    public CommonResult<PageResult<SysOperLog>> list(@ModelAttribute OperLogQuery query) {
        if (!isSuperUser(AuthContext.get())) {
            return error(HttpServletResponse.SC_FORBIDDEN, i18n("auth.permission.denied"));
        }
        return success(page(query, operLogService::selectPage));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("log:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (!isSuperUser(AuthContext.get())) {
            return error(HttpServletResponse.SC_FORBIDDEN, i18n("auth.permission.denied"));
        }
        if (id == null) {
            return success();
        }
        if (!operLogService.removeById(id)) {
            return error(logConstants.getController().getInternalServerErrorCode(),
                    i18n(logConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("log:delete")
    public CommonResult<Void> batchDelete(@RequestBody(required = false) List<Long> ids) {
        if (!isSuperUser(AuthContext.get())) {
            return error(HttpServletResponse.SC_FORBIDDEN, i18n("auth.permission.denied"));
        }
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        if (!operLogService.removeByIds(ids)) {
            return error(logConstants.getController().getInternalServerErrorCode(),
                    i18n(logConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    private boolean isSuperUser(AuthUser user) {
        if (user == null) {
            return false;
        }
        List<String> superUsers = permissionProperties.getSuperUsers();
        if (superUsers == null || superUsers.isEmpty()) {
            return false;
        }
        String userName = user.getUserName();
        if (userName == null) {
            return false;
        }
        String normalized = userName.toLowerCase(Locale.ROOT);
        for (String superUser : superUsers) {
            if (superUser != null && normalized.equals(superUser.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
