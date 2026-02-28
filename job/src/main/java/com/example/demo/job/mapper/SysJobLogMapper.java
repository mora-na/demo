package com.example.demo.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.job.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务执行记录 Mapper。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/28
 */
@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
}
