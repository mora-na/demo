package com.example.demo.dict.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.demo.common.model.CommonResult;
import com.example.demo.common.model.PageResult;
import com.example.demo.common.web.BaseController;
import com.example.demo.common.web.permission.RequirePermission;
import com.example.demo.dict.config.DictConstants;
import com.example.demo.dict.dto.*;
import com.example.demo.dict.entity.DictData;
import com.example.demo.dict.entity.DictType;
import com.example.demo.dict.service.DictDataService;
import com.example.demo.dict.service.DictService;
import com.example.demo.dict.service.DictTypeService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 字典管理后台接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Validated
@RestController
@RequestMapping("/sys/dict")
@RequiredArgsConstructor
public class DictAdminController extends BaseController {

    private final DictTypeService dictTypeService;
    private final DictDataService dictDataService;
    private final DictService dictService;
    private final DictConstants dictConstants;

    @GetMapping("/type/list")
    @RequirePermission("dict:query")
    public CommonResult<PageResult<DictTypeVO>> listTypes(@ModelAttribute DictTypeQuery query) {
        PageResult<DictTypeVO> result = page(query, (page, q) -> dictTypeService.page(page, Wrappers.lambdaQuery(DictType.class)
                        .like(StringUtils.isNotBlank(q.getDictType()), DictType::getDictType, q.getDictType())
                        .like(StringUtils.isNotBlank(q.getDictName()), DictType::getDictName, q.getDictName())
                        .eq(q.getStatus() != null, DictType::getStatus, q.getStatus())
                        .orderByAsc(DictType::getSort)
                        .orderByDesc(DictType::getId)),
                this::toTypeVO);
        return success(result);
    }

    @PostMapping("/type")
    @RequirePermission("dict:create")
    public CommonResult<DictTypeVO> createType(@Valid @RequestBody DictTypeCreateRequest request) {
        if (existsType(request.getDictType(), null)) {
            return error(dictConstants.getController().getBadRequestCode(), i18n(dictConstants.getMessage().getDictTypeExists()));
        }
        DictType type = new DictType();
        type.setDictType(request.getDictType());
        type.setDictName(request.getDictName());
        type.setStatus(normalizeStatus(request.getStatus()));
        type.setSort(request.getSort() == null ? dictConstants.getSort().getDefaultSort() : request.getSort());
        type.setRemark(request.getRemark());
        dictTypeService.save(type);
        dictService.refreshCache(request.getDictType());
        return success(toTypeVO(type));
    }

    @PutMapping("/type/{id}")
    @RequirePermission("dict:update")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> updateType(@PathVariable Long id, @Valid @RequestBody DictTypeUpdateRequest request) {
        DictType existing = dictTypeService.getById(id);
        if (existing == null) {
            return error(dictConstants.getController().getNotFoundCode(), i18n(dictConstants.getMessage().getDictTypeNotFound()));
        }
        if (existsType(request.getDictType(), id)) {
            return error(dictConstants.getController().getBadRequestCode(), i18n(dictConstants.getMessage().getDictTypeExists()));
        }
        String oldType = existing.getDictType();
        DictType type = new DictType();
        type.setId(id);
        type.setDictType(request.getDictType());
        type.setDictName(request.getDictName());
        type.setStatus(normalizeStatus(request.getStatus()));
        type.setSort(request.getSort() == null ? dictConstants.getSort().getDefaultSort() : request.getSort());
        type.setRemark(request.getRemark());
        if (!dictTypeService.updateById(type)) {
            return error(dictConstants.getController().getInternalServerErrorCode(), i18n(dictConstants.getMessage().getCommonUpdateFailed()));
        }
        if (StringUtils.isNotBlank(oldType) && !oldType.equals(request.getDictType())) {
            dictDataService.update(Wrappers.lambdaUpdate(DictData.class)
                    .eq(DictData::getDictType, oldType)
                    .set(DictData::getDictType, request.getDictType()));
            dictService.refreshCache(oldType);
        }
        dictService.refreshCache(request.getDictType());
        return success();
    }

    @DeleteMapping("/type/{id}")
    @RequirePermission("dict:delete")
    @Transactional(rollbackFor = Exception.class)
    public CommonResult<Void> deleteType(@PathVariable Long id) {
        DictType existing = dictTypeService.getById(id);
        if (existing == null) {
            return error(dictConstants.getController().getNotFoundCode(), i18n(dictConstants.getMessage().getDictTypeNotFound()));
        }
        String dictType = existing.getDictType();
        dictDataService.remove(Wrappers.lambdaQuery(DictData.class).eq(DictData::getDictType, dictType));
        if (!dictTypeService.removeById(id)) {
            return error(dictConstants.getController().getInternalServerErrorCode(), i18n(dictConstants.getMessage().getCommonDeleteFailed()));
        }
        dictService.refreshCache(dictType);
        return success();
    }

    @GetMapping("/data/list")
    @RequirePermission("dict:query")
    public CommonResult<PageResult<DictDataVO>> listData(@ModelAttribute DictDataQuery query) {
        PageResult<DictDataVO> result = page(query, (page, q) -> dictDataService.page(page, Wrappers.lambdaQuery(DictData.class)
                        .eq(StringUtils.isNotBlank(q.getDictType()), DictData::getDictType, q.getDictType())
                        .like(StringUtils.isNotBlank(q.getDictLabel()), DictData::getDictLabel, q.getDictLabel())
                        .like(StringUtils.isNotBlank(q.getDictValue()), DictData::getDictValue, q.getDictValue())
                        .eq(q.getStatus() != null, DictData::getStatus, q.getStatus())
                        .orderByAsc(DictData::getSort)
                        .orderByDesc(DictData::getId)),
                this::toDataVO);
        return success(result);
    }

    @PostMapping("/data")
    @RequirePermission("dict:create")
    public CommonResult<DictDataVO> createData(@Valid @RequestBody DictDataCreateRequest request) {
        DictType type = dictTypeService.getOne(Wrappers.lambdaQuery(DictType.class)
                .eq(DictType::getDictType, request.getDictType()));
        if (type == null) {
            return error(dictConstants.getController().getNotFoundCode(), i18n(dictConstants.getMessage().getDictTypeNotFound()));
        }
        if (existsData(request.getDictType(), request.getDictValue(), null)) {
            return error(dictConstants.getController().getBadRequestCode(), i18n(dictConstants.getMessage().getDictDataExists()));
        }
        DictData data = new DictData();
        data.setDictType(request.getDictType());
        data.setDictLabel(request.getDictLabel());
        data.setDictValue(request.getDictValue());
        data.setStatus(normalizeStatus(request.getStatus()));
        data.setSort(request.getSort() == null ? dictConstants.getSort().getDefaultSort() : request.getSort());
        data.setRemark(request.getRemark());
        dictDataService.save(data);
        dictService.refreshCache(request.getDictType());
        return success(toDataVO(data));
    }

    @PutMapping("/data/{id}")
    @RequirePermission("dict:update")
    public CommonResult<Void> updateData(@PathVariable Long id, @Valid @RequestBody DictDataUpdateRequest request) {
        DictData existing = dictDataService.getById(id);
        if (existing == null) {
            return error(dictConstants.getController().getNotFoundCode(), i18n(dictConstants.getMessage().getDictDataNotFound()));
        }
        if (existsData(existing.getDictType(), request.getDictValue(), id)) {
            return error(dictConstants.getController().getBadRequestCode(), i18n(dictConstants.getMessage().getDictDataExists()));
        }
        DictData data = new DictData();
        data.setId(id);
        data.setDictLabel(request.getDictLabel());
        data.setDictValue(request.getDictValue());
        data.setStatus(normalizeStatus(request.getStatus()));
        data.setSort(request.getSort() == null ? dictConstants.getSort().getDefaultSort() : request.getSort());
        data.setRemark(request.getRemark());
        if (!dictDataService.updateById(data)) {
            return error(dictConstants.getController().getInternalServerErrorCode(), i18n(dictConstants.getMessage().getCommonUpdateFailed()));
        }
        dictService.refreshCache(existing.getDictType());
        return success();
    }

    @DeleteMapping("/data/{id}")
    @RequirePermission("dict:delete")
    public CommonResult<Void> deleteData(@PathVariable Long id) {
        DictData existing = dictDataService.getById(id);
        if (existing == null) {
            return error(dictConstants.getController().getNotFoundCode(), i18n(dictConstants.getMessage().getDictDataNotFound()));
        }
        if (!dictDataService.removeById(id)) {
            return error(dictConstants.getController().getInternalServerErrorCode(), i18n(dictConstants.getMessage().getCommonDeleteFailed()));
        }
        dictService.refreshCache(existing.getDictType());
        return success();
    }

    @DeleteMapping("/cache/refresh")
    @RequirePermission("dict:cache:refresh")
    public CommonResult<Void> refreshCache() {
        dictService.refreshCache();
        return success();
    }

    private DictTypeVO toTypeVO(DictType type) {
        DictTypeVO vo = new DictTypeVO();
        vo.setId(type.getId());
        vo.setDictType(type.getDictType());
        vo.setDictName(type.getDictName());
        vo.setStatus(type.getStatus());
        vo.setSort(type.getSort());
        vo.setRemark(type.getRemark());
        vo.setCreateTime(type.getCreateTime());
        return vo;
    }

    private DictDataVO toDataVO(DictData data) {
        DictDataVO vo = new DictDataVO();
        vo.setId(data.getId());
        vo.setDictType(data.getDictType());
        vo.setDictLabel(data.getDictLabel());
        vo.setDictValue(data.getDictValue());
        vo.setStatus(data.getStatus());
        vo.setSort(data.getSort());
        vo.setRemark(data.getRemark());
        vo.setCreateTime(data.getCreateTime());
        return vo;
    }

    private boolean existsType(String dictType, Long excludeId) {
        if (StringUtils.isBlank(dictType)) {
            return false;
        }
        DictType one = dictTypeService.getOne(Wrappers.lambdaQuery(DictType.class)
                .eq(DictType::getDictType, dictType)
                .ne(excludeId != null, DictType::getId, excludeId));
        return one != null;
    }

    private boolean existsData(String dictType, String dictValue, Long excludeId) {
        if (StringUtils.isBlank(dictType) || StringUtils.isBlank(dictValue)) {
            return false;
        }
        DictData one = dictDataService.getOne(Wrappers.lambdaQuery(DictData.class)
                .eq(DictData::getDictType, dictType)
                .eq(DictData::getDictValue, dictValue)
                .ne(excludeId != null, DictData::getId, excludeId));
        return one != null;
    }

    private Integer normalizeStatus(Integer status) {
        return status != null
                && (status == dictConstants.getStatus().getDisabled()
                || status == dictConstants.getStatus().getEnabled())
                ? status
                : dictConstants.getStatus().getEnabled();
    }
}
