package com.example.demo.extension.manager;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.extension.dto.DynamicApiCreateRequest;
import com.example.demo.extension.dto.DynamicApiQuery;
import com.example.demo.extension.dto.DynamicApiUpdateRequest;
import com.example.demo.extension.model.DynamicApi;

import java.util.List;

/**
 * 动态接口管理服务。
 */
public interface DynamicApiService {

    DynamicApi createApi(DynamicApiCreateRequest request);

    DynamicApi updateApi(Long id, DynamicApiUpdateRequest request);

    boolean enableApi(Long id);

    boolean disableApi(Long id);

    boolean deleteApi(Long id);

    boolean reloadAll();

    DynamicApi getApi(Long id);

    IPage<DynamicApi> page(Page<DynamicApi> page, DynamicApiQuery query);

    List<DynamicApi> listEnabled();
}
