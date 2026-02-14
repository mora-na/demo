package com.example.demo.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.log.dto.LoginLogQuery;
import com.example.demo.log.entity.SysLoginLog;
import com.example.demo.log.mapper.SysLoginLogMapper;
import com.example.demo.log.service.SysLoginLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 登录日志服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Service
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<SysLoginLog> selectPage(Page<SysLoginLog> page, LoginLogQuery query) {
        if (page == null) {
            page = new Page<>(1, 10);
        }
        return this.page(page, buildQuery(query));
    }

    private LambdaQueryWrapper<SysLoginLog> buildQuery(LoginLogQuery query) {
        LambdaQueryWrapper<SysLoginLog> wrapper = Wrappers.lambdaQuery(SysLoginLog.class);
        if (query != null) {
            wrapper.like(StringUtils.isNotBlank(query.getUserName()), SysLoginLog::getUserName, query.getUserName())
                    .like(StringUtils.isNotBlank(query.getLoginIp()), SysLoginLog::getLoginIp, query.getLoginIp())
                    .eq(query.getStatus() != null, SysLoginLog::getStatus, query.getStatus())
                    .eq(query.getLoginType() != null, SysLoginLog::getLoginType, query.getLoginType());
            LocalDateTime begin = parseTime(query.getBeginTime());
            LocalDateTime end = parseTime(query.getEndTime());
            wrapper.ge(begin != null, SysLoginLog::getLoginTime, begin)
                    .le(end != null, SysLoginLog::getLoginTime, end);
        }
        wrapper.orderByDesc(SysLoginLog::getLoginTime)
                .orderByDesc(SysLoginLog::getId);
        return wrapper;
    }

    private LocalDateTime parseTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String trimmed = value.trim();
        try {
            return LocalDateTime.parse(trimmed, DEFAULT_FORMATTER);
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(trimmed);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }
}
