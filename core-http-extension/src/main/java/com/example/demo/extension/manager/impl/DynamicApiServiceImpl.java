package com.example.demo.extension.manager.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.extension.config.DynamicApiConstants;
import com.example.demo.extension.dto.DynamicApiCreateRequest;
import com.example.demo.extension.dto.DynamicApiQuery;
import com.example.demo.extension.dto.DynamicApiUpdateRequest;
import com.example.demo.extension.manager.DynamicApiService;
import com.example.demo.extension.model.DynamicApi;
import com.example.demo.extension.model.DynamicApiStatus;
import com.example.demo.extension.registry.DynamicApiMeta;
import com.example.demo.extension.registry.DynamicApiRegistry;
import com.example.demo.extension.repository.DynamicApiMapper;
import com.example.demo.extension.support.DynamicApiException;
import com.example.demo.extension.support.DynamicApiMetaBuilder;
import com.example.demo.extension.support.DynamicApiValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态接口管理实现。
 */
@Service
@RequiredArgsConstructor
public class DynamicApiServiceImpl implements DynamicApiService {

    private final DynamicApiMapper mapper;
    private final DynamicApiRegistry registry;
    private final DynamicApiMetaBuilder metaBuilder;
    private final DynamicApiValidator validator;
    private final DynamicApiConstants constants;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public DynamicApi createApi(DynamicApiCreateRequest request) {
        if (request == null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        DynamicApi api = new DynamicApi();
        api.setPath(StringUtils.trimToEmpty(request.getPath()));
        api.setMethod(StringUtils.trimToEmpty(request.getMethod()));
        api.setType(StringUtils.trimToEmpty(request.getType()));
        api.setConfig(request.getConfig());
        api.setStatus(resolveStatus(request.getStatus(), DynamicApiStatus.DRAFT));
        api.setAuthMode(StringUtils.trimToEmpty(request.getAuthMode()));
        api.setRateLimitPolicy(StringUtils.trimToEmpty(request.getRateLimitPolicy()));
        api.setTimeoutMs(request.getTimeoutMs());
        api.setRemark(request.getRemark());

        validator.validatePathAndMethod(api.getMethod(), api.getPath());
        ensureUnique(api, null);
        DynamicApiMeta meta = metaBuilder.build(api);
        mapper.insert(api);
        if (DynamicApiStatus.ENABLED.name().equalsIgnoreCase(api.getStatus())) {
            registry.register(meta);
        }
        publishChange(api, "create");
        return api;
    }

    @Override
    public DynamicApi updateApi(Long id, DynamicApiUpdateRequest request) {
        if (id == null || request == null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        DynamicApi existing = mapper.selectById(id);
        if (existing == null || isDeleted(existing)) {
            throw new DynamicApiException(constants.getController().getNotFoundCode(),
                    constants.getMessage().getNotFound());
        }
        String oldMethod = existing.getMethod();
        String oldPath = existing.getPath();
        String oldStatus = existing.getStatus();
        existing.setPath(StringUtils.trimToEmpty(request.getPath()));
        existing.setMethod(StringUtils.trimToEmpty(request.getMethod()));
        existing.setType(StringUtils.trimToEmpty(request.getType()));
        existing.setConfig(request.getConfig());
        existing.setStatus(resolveStatus(request.getStatus(), DynamicApiStatus.DRAFT));
        existing.setAuthMode(StringUtils.trimToEmpty(request.getAuthMode()));
        existing.setRateLimitPolicy(StringUtils.trimToEmpty(request.getRateLimitPolicy()));
        existing.setTimeoutMs(request.getTimeoutMs());
        existing.setRemark(request.getRemark());

        validator.validatePathAndMethod(existing.getMethod(), existing.getPath());
        ensureUnique(existing, existing.getId());
        DynamicApiMeta meta = metaBuilder.build(existing);
        mapper.updateById(existing);
        if (DynamicApiStatus.ENABLED.name().equalsIgnoreCase(oldStatus)) {
            registry.remove(oldMethod, oldPath);
        }
        if (DynamicApiStatus.ENABLED.name().equalsIgnoreCase(existing.getStatus())) {
            registry.register(meta);
        }
        publishChange(existing, "update");
        return existing;
    }

    @Override
    public boolean enableApi(Long id) {
        DynamicApi api = requireApi(id);
        api.setStatus(DynamicApiStatus.ENABLED.name());
        mapper.updateById(api);
        registry.register(metaBuilder.build(api));
        publishChange(api, "enable");
        return true;
    }

    @Override
    public boolean disableApi(Long id) {
        DynamicApi api = requireApi(id);
        api.setStatus(DynamicApiStatus.DISABLED.name());
        mapper.updateById(api);
        registry.remove(api.getMethod(), api.getPath());
        publishChange(api, "disable");
        return true;
    }

    @Override
    public boolean deleteApi(Long id) {
        DynamicApi api = requireApi(id);
        api.setStatus(DynamicApiStatus.DELETED.name());
        mapper.updateById(api);
        registry.remove(api.getMethod(), api.getPath());
        publishChange(api, "delete");
        return true;
    }

    @Override
    public boolean reloadAll() {
        List<DynamicApi> enabled = listEnabled();
        List<DynamicApiMeta> metas = new ArrayList<>();
        for (DynamicApi api : enabled) {
            metas.add(metaBuilder.build(api));
        }
        registry.reload(metas);
        return true;
    }

    @Override
    public DynamicApi getApi(Long id) {
        if (id == null) {
            return null;
        }
        return mapper.selectById(id);
    }

    @Override
    public IPage<DynamicApi> page(Page<DynamicApi> page, DynamicApiQuery query) {
        if (page == null) {
            page = new Page<>(1, 10);
        }
        LambdaQueryWrapper<DynamicApi> wrapper = buildQuery(query);
        return mapper.selectPage(page, wrapper);
    }

    @Override
    public List<DynamicApi> listEnabled() {
        return mapper.selectList(Wrappers.lambdaQuery(DynamicApi.class)
                .eq(DynamicApi::getStatus, DynamicApiStatus.ENABLED.name())
                .eq(DynamicApi::getIsDeleted, 0));
    }

    private DynamicApi requireApi(Long id) {
        if (id == null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getConfigInvalid());
        }
        DynamicApi api = mapper.selectById(id);
        if (api == null || isDeleted(api)) {
            throw new DynamicApiException(constants.getController().getNotFoundCode(),
                    constants.getMessage().getNotFound());
        }
        return api;
    }

    private String resolveStatus(String rawStatus, DynamicApiStatus defaultStatus) {
        DynamicApiStatus status = DynamicApiStatus.from(rawStatus);
        if (status == null) {
            return defaultStatus.name();
        }
        return status.name();
    }

    private boolean isDeleted(DynamicApi api) {
        return api != null && (DynamicApiStatus.DELETED.name().equalsIgnoreCase(api.getStatus())
                || (api.getIsDeleted() != null && api.getIsDeleted() == 1));
    }

    private void ensureUnique(DynamicApi api, Long excludeId) {
        if (api == null) {
            return;
        }
        DynamicApi existing = mapper.selectOne(Wrappers.lambdaQuery(DynamicApi.class)
                .eq(DynamicApi::getMethod, api.getMethod())
                .eq(DynamicApi::getPath, api.getPath())
                .eq(DynamicApi::getIsDeleted, 0)
                .ne(excludeId != null, DynamicApi::getId, excludeId)
                .ne(DynamicApi::getStatus, DynamicApiStatus.DELETED.name()));
        if (existing != null) {
            throw new DynamicApiException(constants.getController().getBadRequestCode(),
                    constants.getMessage().getPathInvalid());
        }
    }

    private LambdaQueryWrapper<DynamicApi> buildQuery(DynamicApiQuery query) {
        LambdaQueryWrapper<DynamicApi> wrapper = Wrappers.lambdaQuery(DynamicApi.class);
        if (query != null) {
            wrapper.like(StringUtils.isNotBlank(query.getPath()), DynamicApi::getPath, query.getPath())
                    .eq(StringUtils.isNotBlank(query.getMethod()), DynamicApi::getMethod, query.getMethod())
                    .eq(StringUtils.isNotBlank(query.getStatus()), DynamicApi::getStatus, query.getStatus())
                    .eq(StringUtils.isNotBlank(query.getType()), DynamicApi::getType, query.getType())
                    .eq(StringUtils.isNotBlank(query.getAuthMode()), DynamicApi::getAuthMode, query.getAuthMode());
        }
        wrapper.orderByDesc(DynamicApi::getUpdateTime)
                .orderByDesc(DynamicApi::getId);
        return wrapper;
    }

    private void publishChange(DynamicApi api, String action) {
        if (eventPublisher == null || api == null) {
            return;
        }
        eventPublisher.publishEvent(new DynamicApiChangedEvent(api.getId(), action));
    }

    /**
     * 简单变更事件，占位后续扩展。
     */
    public static class DynamicApiChangedEvent {
        private final Long apiId;
        private final String action;

        public DynamicApiChangedEvent(Long apiId, String action) {
            this.apiId = apiId;
            this.action = action;
        }

        public Long getApiId() {
            return apiId;
        }

        public String getAction() {
            return action;
        }
    }
}
