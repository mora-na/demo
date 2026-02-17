package com.example.demo.extension.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.extension.model.DynamicApi;
import org.apache.ibatis.annotations.Mapper;

/**
 * 动态接口 Mapper。
 */
@Mapper
public interface DynamicApiMapper extends BaseMapper<DynamicApi> {
}
