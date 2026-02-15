package com.example.demo.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.log.dto.OperLogQuery;
import com.example.demo.log.entity.SysOperLog;

/**
 * 操作日志服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public interface SysOperLogService extends IService<SysOperLog> {

    IPage<SysOperLog> selectPage(Page<SysOperLog> page, OperLogQuery query);
}
