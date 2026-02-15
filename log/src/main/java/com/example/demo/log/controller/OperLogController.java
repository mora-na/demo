package com.example.demo.log.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.dto.OperLogQuery;
import com.example.demo.log.entity.SysOperLog;
import com.example.demo.log.service.SysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    @RequirePermission("log:query")
    public CommonResult<PageResult<SysOperLog>> list(@ModelAttribute OperLogQuery query) {
        return success(page(query, operLogService::selectPage));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("log:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
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
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        operLogService.removeByIds(ids);
        return success();
    }
}
