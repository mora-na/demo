package com.example.demo.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.log.dto.DynamicApiLogQuery;
import com.example.demo.log.entity.SysDynamicApiLog;

/**
 * 动态接口日志服务。
 */
public interface SysDynamicApiLogService extends IService<SysDynamicApiLog> {

    IPage<SysDynamicApiLog> selectPage(Page<SysDynamicApiLog> page, DynamicApiLogQuery query);
}
