package com.example.demo.log.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.dto.LoginLogQuery;
import com.example.demo.log.entity.SysLoginLog;
import com.example.demo.log.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 登录日志管理接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@RestController
@RequestMapping("/logs/login")
@RequiredArgsConstructor
public class LoginLogController extends BaseController {

    private final SysLoginLogService loginLogService;
    private final LogConstants logConstants;

    @GetMapping
    @RequirePermission("login-log:query")
    public CommonResult<PageResult<SysLoginLog>> list(@ModelAttribute LoginLogQuery query) {
        return success(page(query, loginLogService::selectPage));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("login-log:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (id == null) {
            return success();
        }
        if (!loginLogService.removeById(id)) {
            return error(logConstants.getController().getInternalServerErrorCode(),
                    i18n(logConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("login-log:delete")
    public CommonResult<Void> batchDelete(@RequestBody(required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        if (!loginLogService.removeByIds(ids)) {
            return error(logConstants.getController().getInternalServerErrorCode(),
                    i18n(logConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }
}
