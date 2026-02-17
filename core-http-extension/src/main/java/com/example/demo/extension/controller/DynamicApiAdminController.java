package com.example.demo.extension.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.dto.DynamicApiCreateRequest;
import com.example.demo.extension.dto.DynamicApiQuery;
import com.example.demo.extension.dto.DynamicApiUpdateRequest;
import com.example.demo.extension.manager.DynamicApiService;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.support.DynamicApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 动态接口管理接口。
 */
@RestController
@RequestMapping("/dynamic-api")
@RequiredArgsConstructor
public class DynamicApiAdminController extends BaseController {

    private final DynamicApiService dynamicApiService;
    private final DynamicApiConstants constants;

    @GetMapping
    @RequirePermission("dynamic-api:query")
    public CommonResult<PageResult<DynamicApi>> list(@ModelAttribute DynamicApiQuery query) {
        PageResult<DynamicApi> result = page(query, dynamicApiService::page);
        return success(result);
    }

    @GetMapping("/{id}")
    @RequirePermission("dynamic-api:query")
    public CommonResult<DynamicApi> detail(@PathVariable Long id) {
        DynamicApi api = dynamicApiService.getApi(id);
        if (api == null) {
            return error(constants.getController().getNotFoundCode(), i18n(constants.getMessage().getNotFound()));
        }
        return success(api);
    }

    @PostMapping
    @RequirePermission("dynamic-api:create")
    public CommonResult<DynamicApi> create(@Validated @RequestBody DynamicApiCreateRequest request) {
        try {
            return success(dynamicApiService.createApi(request));
        } catch (DynamicApiException ex) {
            return error(ex.getCode(), i18n(ex.getMessageKey()));
        }
    }

    @PutMapping("/{id}")
    @RequirePermission("dynamic-api:update")
    public CommonResult<DynamicApi> update(@PathVariable Long id, @Validated @RequestBody DynamicApiUpdateRequest request) {
        try {
            return success(dynamicApiService.updateApi(id, request));
        } catch (DynamicApiException ex) {
            return error(ex.getCode(), i18n(ex.getMessageKey()));
        }
    }

    @PutMapping("/{id}/enable")
    @RequirePermission("dynamic-api:status")
    public CommonResult<Void> enable(@PathVariable Long id) {
        try {
            dynamicApiService.enableApi(id);
            return success();
        } catch (DynamicApiException ex) {
            return error(ex.getCode(), i18n(ex.getMessageKey()));
        }
    }

    @PutMapping("/{id}/disable")
    @RequirePermission("dynamic-api:status")
    public CommonResult<Void> disable(@PathVariable Long id) {
        try {
            dynamicApiService.disableApi(id);
            return success();
        } catch (DynamicApiException ex) {
            return error(ex.getCode(), i18n(ex.getMessageKey()));
        }
    }

    @DeleteMapping("/{id}")
    @RequirePermission("dynamic-api:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        try {
            dynamicApiService.deleteApi(id);
            return success();
        } catch (DynamicApiException ex) {
            return error(ex.getCode(), i18n(ex.getMessageKey()));
        }
    }

    @PostMapping("/reload")
    @RequirePermission("dynamic-api:reload")
    public CommonResult<Void> reload() {
        dynamicApiService.reloadAll();
        return success();
    }
}
