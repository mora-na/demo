package com.example.demo.extension.api.facade;

import com.example.demo.common.model.PageResult;
import com.example.demo.extension.api.dto.DynamicApiCreateCommand;
import com.example.demo.extension.api.dto.DynamicApiDTO;
import com.example.demo.extension.api.dto.DynamicApiQuery;
import com.example.demo.extension.api.dto.DynamicApiUpdateCommand;

import java.util.List;

/**
 * 动态接口管理对外契约。
 */
public interface DynamicApiManageFacade {

    DynamicApiDTO create(DynamicApiCreateCommand command);

    DynamicApiDTO update(Long id, DynamicApiUpdateCommand command);

    boolean enable(Long id);

    boolean disable(Long id);

    boolean delete(Long id);

    boolean reloadAll();

    DynamicApiDTO get(Long id);

    PageResult<DynamicApiDTO> page(DynamicApiQuery query);

    List<DynamicApiDTO> listEnabled();
}
