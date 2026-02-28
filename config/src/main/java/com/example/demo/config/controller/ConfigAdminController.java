package com.example.demo.config.controller;

import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.config.config.ConfigConstants;
import com.example.demo.config.dto.ConfigCreateRequest;
import com.example.demo.config.dto.ConfigQuery;
import com.example.demo.config.dto.ConfigUpdateRequest;
import com.example.demo.config.dto.ConfigVO;
import com.example.demo.config.entity.SysConfig;
import com.example.demo.config.service.ConfigManagerService;
import com.example.demo.config.support.ConfigOperationResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 配置管理后台接口。
 */
@Validated
@RestController
@RequestMapping("/sys/config")
public class ConfigAdminController extends BaseController {

    private final ConfigManagerService configManagerService;
    private final ConfigConstants constants;

    public ConfigAdminController(ConfigManagerService configManagerService, ConfigConstants constants) {
        this.configManagerService = configManagerService;
        this.constants = constants;
    }

    @GetMapping
    @RequirePermission("config:query")
    public CommonResult<PageResult<ConfigVO>> list(@ModelAttribute ConfigQuery query) {
        PageResult<ConfigVO> result = page(query, configManagerService::page, this::toVO);
        return success(result);
    }

    @GetMapping("/{id}")
    @RequirePermission("config:query")
    public CommonResult<ConfigVO> detail(@PathVariable Long id) {
        SysConfig config = configManagerService.getById(id);
        if (config == null) {
            return error(constants.getController().getNotFoundCode(), i18n(constants.getMessage().getConfigNotFound()));
        }
        return success(toVO(config));
    }

    @PostMapping
    @RequirePermission("config:create")
    public CommonResult<ConfigVO> create(@Valid @RequestBody ConfigCreateRequest request) {
        ConfigOperationResult result = configManagerService.create(request);
        if (!result.isSuccess()) {
            String messageKey = result.getErrorMessageKey();
            int code = constants.getController().getBadRequestCode();
            if (constants.getMessage().getCommonUpdateFailed().equals(messageKey)) {
                code = constants.getController().getInternalServerErrorCode();
            }
            return error(code, i18n(messageKey));
        }
        return success(toVO(result.getConfig()));
    }

    @PutMapping("/{id}")
    @RequirePermission("config:update")
    public CommonResult<ConfigVO> update(@PathVariable Long id, @Valid @RequestBody ConfigUpdateRequest request) {
        ConfigOperationResult result = configManagerService.update(id, request);
        if (!result.isSuccess()) {
            String messageKey = result.getErrorMessageKey();
            int code = constants.getController().getBadRequestCode();
            if (constants.getMessage().getConfigNotFound().equals(messageKey)) {
                code = constants.getController().getNotFoundCode();
            } else if (constants.getMessage().getCommonUpdateFailed().equals(messageKey)) {
                code = constants.getController().getInternalServerErrorCode();
            }
            return error(code, i18n(messageKey));
        }
        return success(toVO(result.getConfig()));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("config:delete")
    public CommonResult<Void> delete(@PathVariable Long id) {
        SysConfig existing = configManagerService.getById(id);
        if (existing == null) {
            return error(constants.getController().getNotFoundCode(), i18n(constants.getMessage().getConfigNotFound()));
        }
        if (!configManagerService.delete(id)) {
            return error(constants.getController().getInternalServerErrorCode(), i18n(constants.getMessage().getCommonDeleteFailed()));
        }
        return success();
    }

    @PostMapping("/cache/refresh")
    @RequirePermission("config:cache:refresh")
    public CommonResult<Void> refreshCache(@RequestParam(value = "group", required = false) String group,
                                           @RequestParam(value = "key", required = false) String key) {
        if (key == null || key.trim().isEmpty()) {
            configManagerService.refreshCache();
        } else {
            configManagerService.refreshCache(group, key.trim());
        }
        return success();
    }

    private ConfigVO toVO(SysConfig config) {
        if (config == null) {
            return null;
        }
        ConfigVO vo = new ConfigVO();
        vo.setId(config.getId());
        vo.setGroup(config.getConfigGroup());
        vo.setKey(config.getConfigKey());
        vo.setType(com.example.demo.config.api.enums.ConfigValueType.from(config.getConfigType()));
        vo.setSchema(config.getConfigSchema());
        vo.setStatus(config.getStatus());
        vo.setHotUpdate(config.getHotUpdate());
        vo.setSensitive(config.getConfigSensitive());
        vo.setConfigVersion(config.getConfigVersion());
        vo.setRemark(config.getRemark());
        vo.setCreateTime(config.getCreateTime());
        vo.setUpdateTime(config.getUpdateTime());
        if (config.getConfigSensitive() != null && config.getConfigSensitive() == 1) {
            vo.setValue(constants.getMask().getMaskValue());
        } else {
            vo.setValue(config.getConfigValue());
        }
        return vo;
    }
}
