package com.example.demo.extension.facade;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.model.PageResult;
import com.example.demo.extension.api.dto.DynamicApiCreateCommand;
import com.example.demo.extension.api.dto.DynamicApiDTO;
import com.example.demo.extension.api.dto.DynamicApiQuery;
import com.example.demo.extension.api.dto.DynamicApiUpdateCommand;
import com.example.demo.extension.api.exception.DynamicApiFacadeException;
import com.example.demo.extension.api.facade.DynamicApiManageFacade;
import com.example.demo.extension.dto.DynamicApiCreateRequest;
import com.example.demo.extension.dto.DynamicApiUpdateRequest;
import com.example.demo.extension.manager.DynamicApiService;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.support.DynamicApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 动态接口管理对外契约实现。
 */
@Service
@RequiredArgsConstructor
public class DynamicApiManageFacadeImpl implements DynamicApiManageFacade {

    private final DynamicApiService dynamicApiService;

    @Override
    public DynamicApiDTO create(DynamicApiCreateCommand command) {
        try {
            DynamicApiCreateRequest request = toCreateRequest(command);
            DynamicApi api = dynamicApiService.createApi(request);
            return toDto(api);
        } catch (DynamicApiException ex) {
            throw new DynamicApiFacadeException(ex.getCode(), ex.getMessageKey());
        }
    }

    @Override
    public DynamicApiDTO update(Long id, DynamicApiUpdateCommand command) {
        try {
            DynamicApiUpdateRequest request = toUpdateRequest(command);
            DynamicApi api = dynamicApiService.updateApi(id, request);
            return toDto(api);
        } catch (DynamicApiException ex) {
            throw new DynamicApiFacadeException(ex.getCode(), ex.getMessageKey());
        }
    }

    @Override
    public boolean enable(Long id) {
        try {
            return dynamicApiService.enableApi(id);
        } catch (DynamicApiException ex) {
            throw new DynamicApiFacadeException(ex.getCode(), ex.getMessageKey());
        }
    }

    @Override
    public boolean disable(Long id) {
        try {
            return dynamicApiService.disableApi(id);
        } catch (DynamicApiException ex) {
            throw new DynamicApiFacadeException(ex.getCode(), ex.getMessageKey());
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            return dynamicApiService.deleteApi(id);
        } catch (DynamicApiException ex) {
            throw new DynamicApiFacadeException(ex.getCode(), ex.getMessageKey());
        }
    }

    @Override
    public boolean reloadAll() {
        try {
            return dynamicApiService.reloadAll();
        } catch (DynamicApiException ex) {
            throw new DynamicApiFacadeException(ex.getCode(), ex.getMessageKey());
        }
    }

    @Override
    public DynamicApiDTO get(Long id) {
        DynamicApi api = dynamicApiService.getApi(id);
        return toDto(api);
    }

    @Override
    public PageResult<DynamicApiDTO> page(DynamicApiQuery query) {
        com.example.demo.extension.dto.DynamicApiQuery internalQuery = toInternalQuery(query);
        Page<DynamicApi> page = query == null ? new Page<>(1, 10) : query.buildPage();
        IPage<DynamicApi> result = dynamicApiService.page(page, internalQuery);
        List<DynamicApiDTO> records = toDtoList(result == null ? null : result.getRecords());
        long total = result == null ? 0L : Math.max(result.getTotal(), 0L);
        int pageNum = result == null ? 1 : (int) result.getCurrent();
        long pageSizeValue = result == null ? page.getSize() : result.getSize();
        int pageSize = (int) Math.min(pageSizeValue, Integer.MAX_VALUE);
        return new PageResult<>(total, records, pageNum, pageSize);
    }

    @Override
    public List<DynamicApiDTO> listEnabled() {
        return toDtoList(dynamicApiService.listEnabled());
    }

    private DynamicApiCreateRequest toCreateRequest(DynamicApiCreateCommand command) {
        if (command == null) {
            return null;
        }
        DynamicApiCreateRequest request = new DynamicApiCreateRequest();
        request.setPath(command.getPath());
        request.setMethod(command.getMethod());
        request.setType(command.getType());
        request.setConfig(command.getConfig());
        request.setStatus(command.getStatus());
        request.setAuthMode(command.getAuthMode());
        request.setRateLimitPolicy(command.getRateLimitPolicy());
        request.setTimeoutMs(command.getTimeoutMs());
        request.setRemark(command.getRemark());
        request.setBeanName(command.getBeanName());
        request.setParamMode(command.getParamMode());
        request.setParamSchema(command.getParamSchema());
        request.setSql(command.getSql());
        request.setHttpUrl(command.getHttpUrl());
        request.setHttpMethod(command.getHttpMethod());
        request.setHttpPassHeaders(command.getHttpPassHeaders());
        request.setHttpPassQuery(command.getHttpPassQuery());
        return request;
    }

    private DynamicApiUpdateRequest toUpdateRequest(DynamicApiUpdateCommand command) {
        if (command == null) {
            return null;
        }
        DynamicApiUpdateRequest request = new DynamicApiUpdateRequest();
        request.setPath(command.getPath());
        request.setMethod(command.getMethod());
        request.setType(command.getType());
        request.setConfig(command.getConfig());
        request.setStatus(command.getStatus());
        request.setAuthMode(command.getAuthMode());
        request.setRateLimitPolicy(command.getRateLimitPolicy());
        request.setTimeoutMs(command.getTimeoutMs());
        request.setRemark(command.getRemark());
        request.setBeanName(command.getBeanName());
        request.setParamMode(command.getParamMode());
        request.setParamSchema(command.getParamSchema());
        request.setSql(command.getSql());
        request.setHttpUrl(command.getHttpUrl());
        request.setHttpMethod(command.getHttpMethod());
        request.setHttpPassHeaders(command.getHttpPassHeaders());
        request.setHttpPassQuery(command.getHttpPassQuery());
        return request;
    }

    private com.example.demo.extension.dto.DynamicApiQuery toInternalQuery(DynamicApiQuery query) {
        com.example.demo.extension.dto.DynamicApiQuery internal = new com.example.demo.extension.dto.DynamicApiQuery();
        if (query == null) {
            return internal;
        }
        internal.setPath(query.getPath());
        internal.setMethod(query.getMethod());
        internal.setStatus(query.getStatus());
        internal.setType(query.getType());
        internal.setAuthMode(query.getAuthMode());
        internal.setPageNum(query.getPageNum());
        internal.setPageSize(query.getPageSize());
        internal.setOrderByColumn(query.getOrderByColumn());
        internal.setIsAsc(query.getIsAsc());
        return internal;
    }

    private DynamicApiDTO toDto(DynamicApi api) {
        if (api == null) {
            return null;
        }
        DynamicApiDTO dto = new DynamicApiDTO();
        dto.setId(api.getId());
        dto.setPath(api.getPath());
        dto.setMethod(api.getMethod());
        dto.setStatus(api.getStatus());
        dto.setType(api.getType());
        dto.setConfig(api.getConfig());
        dto.setAuthMode(api.getAuthMode());
        dto.setRateLimitPolicy(api.getRateLimitPolicy());
        dto.setTimeoutMs(api.getTimeoutMs());
        dto.setCreateBy(api.getCreateBy());
        dto.setCreateDept(api.getCreateDept());
        dto.setUpdateBy(api.getUpdateBy());
        dto.setVersion(api.getVersion());
        dto.setRemark(api.getRemark());
        dto.setCreateTime(api.getCreateTime());
        dto.setUpdateTime(api.getUpdateTime());
        return dto;
    }

    private List<DynamicApiDTO> toDtoList(List<DynamicApi> records) {
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream()
                .filter(Objects::nonNull)
                .map(this::toDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
