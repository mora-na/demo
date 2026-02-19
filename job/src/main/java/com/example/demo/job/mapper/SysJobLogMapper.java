package com.example.demo.job.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.job.entity.SysJobLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务日志数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */


@Mapper
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {
}
