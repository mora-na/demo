package com.example.demo.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.notice.entity.Notice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知数据访问层。
 *
 * @author GPT-5.2-codex(high)
 * @date 2026/2/12
 */


@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {
}
