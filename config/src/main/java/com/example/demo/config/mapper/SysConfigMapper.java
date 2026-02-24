package com.example.demo.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.config.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 配置 Mapper。
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
