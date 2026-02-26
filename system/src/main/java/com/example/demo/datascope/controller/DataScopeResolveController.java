package com.example.demo.datascope.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.mybatis.DbDataScopeRuleProvider;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.datascope.config.DataScopeConstants;
import com.example.demo.datascope.dto.DataScopeResolveMenuVO;
import com.example.demo.datascope.dto.DataScopeResolveResponse;
import com.example.demo.datascope.dto.DataScopeRuleCacheMetricsVO;
import com.example.demo.datascope.service.DataScopeResolveService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据范围解析接口，供权限总览使用。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Validated
@RestController
@RequestMapping("/data-scope")
@RequiredArgsConstructor
public class DataScopeResolveController extends BaseController {

    private final DataScopeResolveService resolveService;
    private final DataScopeConstants dataScopeConstants;
    private final ObjectProvider<DbDataScopeRuleProvider> ruleProvider;

    @GetMapping("/resolve")
    @RequirePermission("data-scope:resolve")
    public CommonResult<DataScopeResolveResponse> resolve(@RequestParam("userId") Long userId,
                                                          @RequestParam(value = "permission", required = false) String permission) {
        DataScopeResolveResponse response = resolveService.resolve(userId, permission);
        if (response == null) {
            return error(dataScopeConstants.getController().getNotFoundCode(), i18n("user.not.found"));
        }
        return success(response);
    }

    @GetMapping("/resolve-all")
    @RequirePermission("data-scope:resolve")
    public CommonResult<List<DataScopeResolveMenuVO>> resolveAll(@RequestParam("userId") Long userId) {
        return success(resolveService.resolveAll(userId));
    }

    @GetMapping("/metrics")
    @RequirePermission("data-scope:resolve")
    public CommonResult<DataScopeRuleCacheMetricsVO> metrics() {
        DbDataScopeRuleProvider provider = ruleProvider.getIfAvailable();
        if (provider == null) {
            return success(new DataScopeRuleCacheMetricsVO());
        }
        return success(provider.snapshotCacheMetrics());
    }
}
