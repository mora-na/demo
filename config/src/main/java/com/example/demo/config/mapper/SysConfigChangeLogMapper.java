package com.example.demo.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.config.entity.SysConfigChangeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 配置变更流水访问层。
 */
@Mapper
public interface SysConfigChangeLogMapper extends BaseMapper<SysConfigChangeLog> {

    @Select("SELECT * FROM demo_config.sys_config_change_log WHERE id > #{lastId} ORDER BY id ASC LIMIT #{limit}")
    List<SysConfigChangeLog> selectAfterId(@Param("lastId") long lastId, @Param("limit") int limit);
}
