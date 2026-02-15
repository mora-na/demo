package com.example.demo.datascope.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.datascope.config.DataScopeConstants;
import com.example.demo.datascope.dto.DataScopeRuleCreateRequest;
import com.example.demo.datascope.dto.DataScopeRuleQuery;
import com.example.demo.datascope.dto.DataScopeRuleUpdateRequest;
import com.example.demo.datascope.dto.DataScopeRuleVO;
import com.example.demo.datascope.entity.DataScopeRule;
import com.example.demo.datascope.service.DataScopeRuleService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 数据范围字段映射管理接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/13
 */
@Validated
@RestController
@RequestMapping("/data-scope-mapping")
@RequiredArgsConstructor
public class DataScopeRuleAdminController extends BaseController {

    private final DataScopeRuleService dataScopeRuleService;
    private final DataScopeConstants dataScopeConstants;

    @GetMapping("/list")
    @RequirePermission("data-scope:rule:query")
    public CommonResult<PageResult<DataScopeRuleVO>> list(@ModelAttribute DataScopeRuleQuery query) {
        return success(page(page -> dataScopeRuleService.page(page, Wrappers.lambdaQuery(DataScopeRule.class)
                .like(StringUtils.isNotBlank(query.getScopeKey()), DataScopeRule::getScopeKey, query.getScopeKey())
                .like(StringUtils.isNotBlank(query.getTableName()), DataScopeRule::getTableName, query.getTableName())
                .orderByAsc(DataScopeRule::getId)), this::toVO));
    }

    @PostMapping
    @RequirePermission("data-scope:rule:create")
    public CommonResult<DataScopeRuleVO> create(@Valid @RequestBody DataScopeRuleCreateRequest request) {
        if (existsScopeKey(request.getScopeKey(), null)) {
            return error(dataScopeConstants.getController().getBadRequestCode(), i18n("data.scope.rule.exists"));
        }
        DataScopeRule rule = new DataScopeRule();
        rule.setScopeKey(request.getScopeKey());
        rule.setTableName(request.getTableName());
        rule.setTableAlias(request.getTableAlias());
        rule.setDeptColumn(request.getDeptColumn());
        rule.setUserColumn(request.getUserColumn());
        rule.setFilterType(normalizeFilterType(request.getFilterType()));
        rule.setStatus(normalizeStatus(request.getStatus()));
        rule.setRemark(request.getRemark());
        dataScopeRuleService.save(rule);
        return success(toVO(rule));
    }

    @PutMapping("/{id}")
    @RequirePermission("data-scope:rule:update")
    public CommonResult<Void> update(@PathVariable Long id, @Valid @RequestBody DataScopeRuleUpdateRequest request) {
        DataScopeRule existing = dataScopeRuleService.getById(id);
        if (existing == null) {
            return error(dataScopeConstants.getController().getNotFoundCode(), i18n("data.scope.rule.not.found"));
        }
        if (StringUtils.isNotBlank(request.getScopeKey()) && existsScopeKey(request.getScopeKey(), id)) {
            return error(dataScopeConstants.getController().getBadRequestCode(), i18n("data.scope.rule.exists"));
        }
        DataScopeRule rule = getDataScopeRule(id, request);
        if (!dataScopeRuleService.updateById(rule)) {
            return error(dataScopeConstants.getController().getInternalServerErrorCode(), i18n("common.update.failed"));
        }
        return success();
    }

    private @NonNull DataScopeRule getDataScopeRule(Long id, DataScopeRuleUpdateRequest request) {
        DataScopeRule rule = new DataScopeRule();
        rule.setId(id);
        rule.setScopeKey(request.getScopeKey());
        rule.setTableName(request.getTableName());
        rule.setTableAlias(request.getTableAlias());
        rule.setDeptColumn(request.getDeptColumn());
        rule.setUserColumn(request.getUserColumn());
        rule.setFilterType(normalizeFilterType(request.getFilterType()));
        rule.setStatus(normalizeStatus(request.getStatus()));
        rule.setRemark(request.getRemark());
        return rule;
    }

    @DeleteMapping("/{id}")
    @RequirePermission("data-scope:rule:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> delete(@PathVariable Long id) {
        if (dataScopeRuleService.getById(id) == null) {
            return error(dataScopeConstants.getController().getNotFoundCode(), i18n("data.scope.rule.not.found"));
        }
        if (!dataScopeRuleService.removeById(id)) {
            return error(dataScopeConstants.getController().getInternalServerErrorCode(), i18n("common.delete.failed"));
        }
        return success();
    }

    private boolean existsScopeKey(String scopeKey, Long excludeId) {
        if (StringUtils.isBlank(scopeKey)) {
            return false;
        }
        DataScopeRule one = dataScopeRuleService.getOne(Wrappers.lambdaQuery(DataScopeRule.class)
                .eq(DataScopeRule::getScopeKey, scopeKey)
                .ne(excludeId != null, DataScopeRule::getId, excludeId));
        return one != null;
    }

    private Integer normalizeStatus(Integer status) {
        if (status == null) {
            return dataScopeConstants.getStatus().getEnabled();
        }
        if (status != dataScopeConstants.getStatus().getDisabled()
                && status != dataScopeConstants.getStatus().getEnabled()) {
            return dataScopeConstants.getStatus().getEnabled();
        }
        return status;
    }

    private Integer normalizeFilterType(Integer filterType) {
        if (filterType == null) {
            return dataScopeConstants.getFilter().getDefaultType();
        }
        if (filterType < dataScopeConstants.getFilter().getTypeMin()
                || filterType > dataScopeConstants.getFilter().getTypeMax()) {
            return dataScopeConstants.getFilter().getDefaultType();
        }
        return filterType;
    }

    private DataScopeRuleVO toVO(DataScopeRule rule) {
        if (rule == null) {
            return null;
        }
        DataScopeRuleVO vo = new DataScopeRuleVO();
        vo.setId(rule.getId());
        vo.setScopeKey(rule.getScopeKey());
        vo.setTableName(rule.getTableName());
        vo.setTableAlias(rule.getTableAlias());
        vo.setDeptColumn(rule.getDeptColumn());
        vo.setUserColumn(rule.getUserColumn());
        vo.setFilterType(rule.getFilterType());
        vo.setStatus(rule.getStatus());
        vo.setRemark(rule.getRemark());
        return vo;
    }
}
