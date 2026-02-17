package com.example.demo.log.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.dto.DynamicApiLogQuery;
import com.example.demo.log.entity.SysDynamicApiLog;
import com.example.demo.log.service.SysDynamicApiLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态接口日志管理接口。
 */
@RestController
@RequestMapping("/logs/dynamic-api")
@RequiredArgsConstructor
public class DynamicApiLogController extends BaseController {

    private final SysDynamicApiLogService logService;
    private final LogConstants logConstants;

    @GetMapping
    @RequirePermission("dynamic-api-log:query")
    public CommonResult<PageResult<SysDynamicApiLog>> list(@ModelAttribute DynamicApiLogQuery query) {
        return success(page(query, logService::selectPage));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("dynamic-api-log:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (id == null) {
            return success();
        }
        if (!logService.removeById(id)) {
            return error(logConstants.getController().getInternalServerErrorCode(),
                    i18n(logConstants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/batch-delete")
    @RequirePermission("dynamic-api-log:delete")
    public CommonResult<Void> batchDelete(@RequestBody(required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return success();
        }
        logService.removeByIds(ids);
        return success();
    }
}
