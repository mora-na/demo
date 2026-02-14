package com.example.demo.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.log.dto.LoginLogQuery;
import com.example.demo.log.entity.SysLoginLog;

/**
 * 登录日志服务接口。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
public interface SysLoginLogService extends IService<SysLoginLog> {

    IPage<SysLoginLog> selectPage(Page<SysLoginLog> page, LoginLogQuery query);
}
