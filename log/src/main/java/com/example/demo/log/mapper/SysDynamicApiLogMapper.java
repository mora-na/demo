package com.example.demo.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.log.entity.SysDynamicApiLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态接口日志 Mapper。
 */
@Mapper
public interface SysDynamicApiLogMapper extends BaseMapper<SysDynamicApiLog> {
}
