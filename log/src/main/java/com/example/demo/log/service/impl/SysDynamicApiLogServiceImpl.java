package com.example.demo.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.log.config.LogConstants;
import com.example.demo.log.dto.DynamicApiLogQuery;
import com.example.demo.log.entity.SysDynamicApiLog;
import com.example.demo.log.mapper.SysDynamicApiLogMapper;
import com.example.demo.log.service.SysDynamicApiLogService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 动态接口日志服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysDynamicApiLogServiceImpl extends ServiceImpl<SysDynamicApiLogMapper, SysDynamicApiLog>
        implements SysDynamicApiLogService {

    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern(LogConstants.Query.DEFAULT_DATE_TIME_PATTERN);

    private final LogConstants logConstants;

    @Override
    public IPage<SysDynamicApiLog> selectPage(Page<SysDynamicApiLog> page, DynamicApiLogQuery query) {
        if (page == null) {
            page = new Page<>(logConstants.getPage().getDefaultPageNum(), logConstants.getPage().getDefaultPageSize());
        }
        return this.page(page, buildQuery(query));
    }

    private LambdaQueryWrapper<SysDynamicApiLog> buildQuery(DynamicApiLogQuery query) {
        LambdaQueryWrapper<SysDynamicApiLog> wrapper = Wrappers.lambdaQuery(SysDynamicApiLog.class);
        if (query != null) {
            wrapper.eq(query.getApiId() != null, SysDynamicApiLog::getApiId, query.getApiId())
                    .like(StringUtils.isNotBlank(query.getApiPath()), SysDynamicApiLog::getApiPath, query.getApiPath())
                    .eq(StringUtils.isNotBlank(query.getApiMethod()), SysDynamicApiLog::getApiMethod, query.getApiMethod())
                    .eq(query.getStatus() != null, SysDynamicApiLog::getStatus, query.getStatus())
                    .like(StringUtils.isNotBlank(query.getUserName()), SysDynamicApiLog::getUserName, query.getUserName());
            LocalDateTime begin = parseTime(query.getBeginTime());
            LocalDateTime end = parseTime(query.getEndTime());
            wrapper.ge(begin != null, SysDynamicApiLog::getRequestTime, begin)
                    .le(end != null, SysDynamicApiLog::getRequestTime, end);
        }
        wrapper.orderByDesc(SysDynamicApiLog::getRequestTime)
                .orderByDesc(SysDynamicApiLog::getId);
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
