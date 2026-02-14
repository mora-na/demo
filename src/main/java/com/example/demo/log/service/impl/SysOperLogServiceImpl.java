package com.example.demo.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.log.dto.OperLogQuery;
import com.example.demo.log.entity.SysOperLog;
import com.example.demo.log.mapper.SysOperLogMapper;
import com.example.demo.log.service.SysOperLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志服务实现。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/14
 */
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public IPage<SysOperLog> selectPage(Page<SysOperLog> page, OperLogQuery query) {
        if (page == null) {
            page = new Page<>(1, 10);
        }
        return this.page(page, buildQuery(query));
    }

    private LambdaQueryWrapper<SysOperLog> buildQuery(OperLogQuery query) {
        LambdaQueryWrapper<SysOperLog> wrapper = Wrappers.lambdaQuery(SysOperLog.class);
        if (query != null) {
            wrapper.like(StringUtils.isNotBlank(query.getUserName()), SysOperLog::getUserName, query.getUserName())
                    .like(StringUtils.isNotBlank(query.getTitle()), SysOperLog::getTitle, query.getTitle())
                    .eq(query.getStatus() != null, SysOperLog::getStatus, query.getStatus())
                    .eq(query.getBusinessType() != null, SysOperLog::getBusinessType, query.getBusinessType());
            LocalDateTime begin = parseTime(query.getBeginTime());
            LocalDateTime end = parseTime(query.getEndTime());
            wrapper.ge(begin != null, SysOperLog::getOperTime, begin)
                    .le(end != null, SysOperLog::getOperTime, end);
        }
        wrapper.orderByDesc(SysOperLog::getOperTime)
                .orderByDesc(SysOperLog::getId);
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
