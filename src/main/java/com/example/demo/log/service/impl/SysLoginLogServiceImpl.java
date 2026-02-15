package com.example.demo.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.dto.LoginLogQuery;
import com.example.demo.log.entity.SysLoginLog;
import com.example.demo.log.mapper.SysLoginLogMapper;
import com.example.demo.log.service.SysLoginLogService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class SysLoginLogServiceImpl extends ServiceImpl<SysLoginLogMapper, SysLoginLog> implements SysLoginLogService {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern(LogConstants.Query.DEFAULT_DATE_TIME_PATTERN);

    private final LogConstants logConstants;

    @Override
    public IPage<SysLoginLog> selectPage(Page<SysLoginLog> page, LoginLogQuery query) {
        if (page == null) {
            page = new Page<>(logConstants.getPage().getDefaultPageNum(), logConstants.getPage().getDefaultPageSize());
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
            return LocalDateTime.parse(trimmed, resolveFormatter());
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(trimmed);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    private DateTimeFormatter resolveFormatter() {
        String pattern = logConstants.getQuery().getDateTimePattern();
        if (StringUtils.isBlank(pattern)) {
            return DEFAULT_FORMATTER;
        }
        try {
            return DateTimeFormatter.ofPattern(pattern);
        } catch (Exception ignored) {
            return DEFAULT_FORMATTER;
        }
    }
}
